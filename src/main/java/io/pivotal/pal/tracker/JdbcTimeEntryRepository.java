package io.pivotal.pal.tracker;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.Date;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private final SimpleJdbcInsert jdbcInsert;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcTimeEntryRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("time_entries")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("project_id", timeEntry.getProjectId());
        sqlParameterSource.addValue("user_id", timeEntry.getUserId());
        sqlParameterSource.addValue("date", timeEntry.getDate());
        sqlParameterSource.addValue("hours", timeEntry.getHours());
        Number id = jdbcInsert.executeAndReturnKey(sqlParameterSource);
        timeEntry.setId(id.longValue());
        return timeEntry;
    }

    @Override
    public TimeEntry find(long id) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * " +
                    "FROM time_entries " +
                    "WHERE id = ?", new Object[]{id}, (rs, rowNum) ->
                    new TimeEntry(
                        rs.getLong("id"),
                        rs.getLong("project_id"),
                        rs.getLong("user_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("hours")));
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public List<TimeEntry> list() {
        return jdbcTemplate.query(
            "SELECT * " +
                "FROM time_entries", (rs, rowNum) ->
                new TimeEntry(
                    rs.getLong("id"),
                    rs.getLong("project_id"),
                    rs.getLong("user_id"),
                    rs.getDate("date").toLocalDate(),
                    rs.getInt("hours")));
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update(
            "UPDATE time_entries " +
                "SET " +
                "project_id = ?, " +
                "user_id = ?, " +
                "date = ?, " +
                "hours = ? " +
                "WHERE id = ?",
            ps -> {
                ps.setLong(1, timeEntry.getProjectId());
                ps.setLong(2, timeEntry.getUserId());
                ps.setDate(3, Date.valueOf(timeEntry.getDate()));
                ps.setInt(4, timeEntry.getHours());
                ps.setLong(5, id);
            }
        );
        timeEntry.setId(id);
        return timeEntry;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(String.format(
            "DELETE FROM time_entries " +
                "WHERE id = %d", id));
    }
}
