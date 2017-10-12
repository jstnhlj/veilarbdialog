package no.nav.fo.veilarbdialog.db;

import lombok.SneakyThrows;
import no.nav.metrics.MetodeTimer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Optional.ofNullable;

@Component
public class Database {

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public <T> List<T> query(String sql, Mapper<T> mapper, Object... args) {
        return time(sql, () -> jdbcTemplate.query(sql, mapper, args));
    }

    public <T> List<T> queryWithNamedParam(String sql, Mapper<T> mapper, Map<String, Object> map) {
        return namedParameterJdbcTemplate.query(sql, map, mapper);
    }
    
    public int update(String sql, Object... args) {
        return time(sql, () -> jdbcTemplate.update(sql, args));
    }

    public <T> T queryForObject(String sql, Mapper<T> mapper, Object... args) {
        return time(sql, () -> jdbcTemplate.queryForObject(sql, mapper, args));
    }

    public long nesteFraSekvens(String sekvensNavn) {
        return jdbcTemplate.queryForObject("select " + sekvensNavn + ".nextval from dual", Long.class);
    }

    public static Date hentDato(ResultSet rs, String kolonneNavn) throws SQLException {
        return ofNullable(rs.getTimestamp(kolonneNavn))
                .map(Timestamp::getTime)
                .map(Date::new)
                .orElse(null);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <T> T time(String sql, Callable<T> callable) {
        return (T) MetodeTimer.timeMetode(callable::call, timerNavn(sql));
    }

    private String timerNavn(String sql) {
        return (sql + ".db").replaceAll("[^\\w]","-");
    }

    @FunctionalInterface
    public interface Mapper<T> extends RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;

        @Override
        default T mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return map(resultSet);
        }

    }

}
