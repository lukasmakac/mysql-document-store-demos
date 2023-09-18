package com.boyzoid.service;

import com.boyzoid.config.DocumentStoreConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.xdevapi.*;
import jakarta.inject.Singleton;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ScoresService {

    private static final String COLLECTION = "scores";
    private final ObjectMapper objectMapper;
    private final Client cli;
    private final DocumentStoreConfig config;


    public ScoresService(DocumentStoreConfig documentStoreConfig, ObjectMapper objectMapper) throws JsonProcessingException {
        this.config = documentStoreConfig;
        this.objectMapper = objectMapper;
        this.cli = new ClientFactory().getClient(documentStoreConfig.getUrl(), objectMapper.writeValueAsString(documentStoreConfig.getPooling()));
    }

    public ArrayList<Object> getAllScores() throws JsonProcessingException {
        final var session = cli.getSession();
        Collection col = getCollection(session);
        DocResult result = col.find().execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getScores(Integer limit, Integer offset) throws JsonProcessingException {
        final var session = cli.getSession();
        Collection col = getCollection(session);
        DocResult result = col.find()
                .limit(limit)
                .offset(offset)
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }


    public ArrayList<Object> getBestScores(Integer limit) throws JsonProcessingException {
        final var session = cli.getSession();
        DocResult result = getCollection(session).find()
                .fields("""
                        firstName as firstName,
                        lastName as lastName,
                        score as score,
                        course as course,
                        `date` as datePlayed
                        """)
                .sort("score asc, `date` desc")
                .limit(limit)
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getRoundsUnderPar() throws JsonProcessingException {
        final var session = cli.getSession();
        DocResult result = getCollection(session).find("score < course.par")
                .fields("""
                        firstName as firstName,
                        lastName as lastName,
                        score as score,
                        course.name as course,
                        `date` as datePlayed
                        """)
                .sort("`date` desc")
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getByScore(Integer score) throws JsonProcessingException {
        final var session = cli.getSession();
        DocResult result = getCollection(session).find("score = :scoreParam")
                .bind("scoreParam", score)
                .fields("""
                        concat(firstName, " ", lastName) as golfer,
                        score as score,
                        course.name as course,
                        `date` as datePlayed
                        """)
                .sort("score asc, `date` desc")
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getByGolfer(String lastName) throws JsonProcessingException {
        final var session = cli.getSession();
        DocResult result = getCollection(session).find("lower(lastName) like :lastNameParam")
                .bind("lastNameParam", lastName.toLowerCase() + "%")
                .sort("lastName, firstName, `date` desc")
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getCourseScoringData() throws JsonProcessingException {
        final var session = cli.getSession();
        DocResult result = getCollection(session).find()
                .fields("""
                        course.name as courseName,
                        round(avg(score), 2)  as avg,
                        min(cast(score as unsigned)) as lowestScore,
                        max(cast(score as unsigned)) as highestScore,
                        count(score) as numberOfRounds
                        """)
                .groupBy("course.name")
                .sort("course.name desc")
                .execute();
        session.close();
        return cleanResults(result.fetchAll());
    }

    public ArrayList<Object> getAggregateCourseScore() throws JsonProcessingException {
        String sql = """
                WITH aggScores AS
                            (SELECT doc ->> '$.course.name' course,
                                MIN(score)              minScore,
                                MAX(score)              maxScore,
                                number
                            FROM scores,
                                JSON_TABLE(doc, '$.holeScores[*]'
                                    COLUMNS (
                                        score INT PATH '$.score',
                                        number INT PATH '$.number')) AS scores
                            GROUP BY course, number
                            ORDER BY course, number)
                        SELECT JSON_OBJECT('courseName', course , 'bestScore', sum(minScore)) agg
                        FROM aggScores
                        GROUP BY course
                        ORDER BY course;
                """;
        final var session = cli.getSession();
        SqlStatement query = session.sql(sql);
        SqlResult result = query.execute();
        session.close();
        return cleanSqlResults(result.fetchAll(), "agg");
    }

    public Boolean addScore(String score) {
        boolean success = true;
        final var session = cli.getSession();
        Collection col = getCollection(session);
        try {
            col.add(score).execute();
        } catch (Exception e) {
            success = false;
        } finally {
            session.close();
        }
        return success;
    }

    public Boolean addHoleScores(String data) throws IOException {
        DbDoc doc = JsonParser.parseDoc(data);
        JsonArray holeScores = JsonParser.parseArray(new StringReader(doc.get("holeScores").toString()));
        var id = doc.get("_id");
        boolean success = true;
        final var session = cli.getSession();
        Collection col = getCollection(session);
        try {
            col.modify("_id = :idParam")
                    .set("holeScores", holeScores)
                    .bind("idParam", id)
                    .execute();
        } catch (Exception e) {
            success = false;
        } finally {
            session.close();
        }
        return success;
    }

    public Boolean removeScore(String id) {
        boolean success = true;
        final var session = cli.getSession();
        try {
            getCollection(session).remove("_id = :idParam")
                    .bind("idParam", id)
                    .execute();
        } catch (Exception e) {
            success = false;
        } finally {
            session.close();
        }
        return success;
    }


    private Collection getCollection(Session session) {
        return session.getSchema(config.getSchema()).getCollection(ScoresService.COLLECTION);
    }

    private ArrayList<Object> cleanResults(List<DbDoc> docs) throws JsonProcessingException {
        ArrayList<Object> cleaned = new ArrayList<>();
        for (DbDoc doc : docs) {
            cleaned.add(objectMapper.readTree(doc.toString()));
        }
        return cleaned;
    }

    private ArrayList<Object> cleanSqlResults(List<Row> rows, String field) throws JsonProcessingException {
        ArrayList<Object> cleaned = new ArrayList<>();
        for (Row row : rows) {
            cleaned.add(objectMapper.readTree(row.getString(field)));
        }
        return cleaned;
    }

    @PreDestroy
    void close() {
        this.cli.close();
    }

}
