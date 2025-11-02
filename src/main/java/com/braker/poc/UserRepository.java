package com.braker.poc;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Long> {
	
	@Query(value = "SELECT bb.NAME substationCode, bb.DESCRIPTION substation, aa.NAME feederCode, aa.DESCRIPTION feeder,\r\n"
			+ "          xx.status, xx.last_change_time_dt AS lastChangeTimeDt, xx.Duration,\r\n"
			+ "          xx.last_change_reason_desc AS lastChangeReasonDesc, xx.tag_status AS tagStatus,\r\n"
			+ "          xx.rtu_err_flag AS rtuErrFlag\r\n"
			+ "FROM (SELECT\r\n"
			+ "			name,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN double_value = 1 THEN 'Open'\r\n"
			+ "			    WHEN double_value = 2 THEN 'Close'\r\n"
			+ "			    ELSE 'Error'\r\n"
			+ "			END AS status,\r\n"
			+ "			TO_CHAR(\r\n"
			+ "			    (TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')),\r\n"
			+ "			    'DD-MM-YYYY HH24:MI:SS'\r\n"
			+ "			) AS last_change_time_dt,\r\n"
			+ "			(EXTRACT(DAY FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Days : '||\r\n"
			+ "			   EXTRACT(HOUR FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND')))) || ' Hour : '||\r\n"
			+ "			   (EXTRACT(MINUTE FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Minute : '||\r\n"
			+ "			   round(EXTRACT(SECOND FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Second' AS Duration,\r\n"
			+ "			last_change_reason_desc,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN tag_flag = 1 THEN 'Enabled'\r\n"
			+ "			    ELSE 'Disabled'\r\n"
			+ "			END AS tag_status,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN rtu_err_flag = 1 THEN 'Error'\r\n"
			+ "			    ELSE 'Healthy'\r\n"
			+ "			END AS rtu_err_flag,\r\n"
			+ "			(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')) last_change_dt\r\n"
			+ "			FROM breaker_value_tab) xx, TS_PARAM_TAB aa, SUBSTATION_TAB bb\r\n"
			+ "	WHERE (:substation = 'ALL' OR bb.NAME = :substation)\r\n"
			+ "	  AND (:braker = 'ALL' OR aa.NAME = :braker)\r\n"
			+ "	  AND (:status = 'ALL' OR xx.status = :status)\r\n"
			+ "	  AND (:changeReason = 'ALL' OR xx.last_change_reason_desc = :changeReason)\r\n"
			+ "	  AND (:tagStatus = 'ALL' OR xx.tag_status = :tagStatus)\r\n"
			+ "	  AND (:rtuErrorFlag = 'ALL' OR xx.rtu_err_flag = :rtuErrorFlag)\r\n"
			+ "	  AND TRUNC(xx.last_change_dt) BETWEEN TO_DATE(:startDate, 'YYYY-MM-DD') AND TO_DATE(:endDate, 'YYYY-MM-DD')\r\n"
			+ "	  AND trim(xx.name) = trim(aa.NAME)\r\n"
			+ "	  AND trim(aa.OF_SUBSTATION)  = trim(bb.NAME)\r\n"
			+ "      AND aa.\"TYPE\"  = 300\r\n"
			+ "      AND aa.RTU_NO <> -1\r\n"
			+ "      AND aa.TS_NO <> -1\r\n"
			+ "    ORDER BY last_change_dt DESC", nativeQuery = true)
	List<BrakerI> getBrakerData(String substation, String braker, String status, String changeReason, String tagStatus, String rtuErrorFlag,
			String startDate, String endDate);
	
	@Query(value = "SELECT DISTINCT bb.NAME substation_code, bb.DESCRIPTION substation\n"
			+ "FROM TS_PARAM_TAB aa, SUBSTATION_TAB bb\n"
			+ "WHERE aa.OF_SUBSTATION  = bb.NAME\n"
			+ "AND aa.\"TYPE\"  = 300\n"
			+ "AND aa.RTU_NO <> -1\n"
			+ "AND aa.TS_NO <> -1\n"
			+ "ORDER BY bb.DESCRIPTION", nativeQuery = true)
	List<BrakerI> getSubstation();
	
	@Query(value = "SELECT aa.NAME feeder_code, aa.DESCRIPTION feeder\n"
			+ "FROM TS_PARAM_TAB aa, SUBSTATION_TAB bb\n"
			+ "WHERE aa.OF_SUBSTATION  = bb.NAME\n"
			+ "AND (:substation = 'ALL' OR bb.NAME = :substation)\n"
			+ "AND aa.\"TYPE\"  = 300\n"
			+ "AND aa.RTU_NO <> -1\n"
			+ "AND aa.TS_NO <> -1\n"
			+ "ORDER BY aa.DESCRIPTION", nativeQuery = true)
	List<BrakerI> getFeeder(String substation);
	
	@Query(value = "SELECT count(aa.NAME) Total_feeder,\r\n"
			+ "       sum(CASE WHEN xx.status = 'Open' THEN 1 ELSE 0 end) AS open_feeder,\r\n"
			+ "       sum(CASE WHEN xx.status = 'Close' THEN 1 ELSE 0 end) AS close_feeder,\r\n"
			+ "       sum(CASE WHEN xx.status = 'Error' THEN 1 ELSE 0 end) AS Error_feeder\r\n"
			+ "FROM (SELECT name,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN double_value = 1 THEN 'Open'\r\n"
			+ "			    WHEN double_value = 2 THEN 'Close'\r\n"
			+ "			    ELSE 'Error'\r\n"
			+ "			END AS status,\r\n"
			+ "			TO_CHAR(\r\n"
			+ "			    (TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')),\r\n"
			+ "			    'DD-MM-YYYY HH24:MI:SS'\r\n"
			+ "			) AS last_change_time_dt,\r\n"
			+ "			(EXTRACT(DAY FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Days : '||\r\n"
			+ "			   EXTRACT(HOUR FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND')))) || ' Hour : '||\r\n"
			+ "			   (EXTRACT(MINUTE FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Minute : '||\r\n"
			+ "			   round(EXTRACT(SECOND FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\r\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Second' AS Duration,\r\n"
			+ "			last_change_reason_desc,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN tag_flag = 1 THEN 'Enabled'\r\n"
			+ "			    ELSE 'Disabled'\r\n"
			+ "			END AS tag_status,\r\n"
			+ "			CASE\r\n"
			+ "			    WHEN rtu_err_flag = 1 THEN 'Error'\r\n"
			+ "			    ELSE 'Healthy'\r\n"
			+ "			END AS rtu_err_flag,\r\n"
			+ "			(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')) last_change_dt\r\n"
			+ "			FROM breaker_value_tab) xx, TS_PARAM_TAB aa, SUBSTATION_TAB bb\r\n"
			+ "WHERE (:substation = 'ALL' OR bb.NAME = :substation)\r\n"
			+ "AND (:braker = 'ALL' OR aa.NAME = :braker)\r\n"
			+ "AND (:status = 'ALL' OR xx.status = :status)\r\n"
			+ "AND (:changeReason = 'ALL' OR xx.last_change_reason_desc = :changeReason)\r\n"
			+ "AND (:tagStatus = 'ALL' OR xx.tag_status = :tagStatus)\r\n"
			+ "AND (:rtuErrorFlag = 'ALL' OR xx.rtu_err_flag = :rtuErrorFlag)\r\n"
			+ "AND TRUNC(xx.last_change_dt) BETWEEN TO_DATE(:startDate, 'YYYY-MM-DD') AND TO_DATE(:endDate, 'YYYY-MM-DD')\r\n"
			+ "AND trim(xx.name) = trim(aa.NAME)\r\n"
			+ "AND trim(aa.OF_SUBSTATION)  = trim(bb.NAME)\r\n"
			+ "AND aa.\"TYPE\"  = 300\r\n"
			+ "AND aa.RTU_NO <> -1\r\n"
			+ "AND aa.TS_NO <> -1", nativeQuery = true)
	List<FeederSummaryI> getFeederSummary(String substation, String braker, String status, String changeReason, String tagStatus, String rtuErrorFlag,
			String startDate, String endDate);

	Optional<Users> findByUsername(String username);
}
