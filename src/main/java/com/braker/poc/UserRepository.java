package com.braker.poc;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Long> {
	
	@Query(value = "SELECT xx.substation_code substationCode, bb.DESCRIPTION substation, xx.feeder_code feederCode, aa.DESCRIPTION feeder,\r\n"
			+ "       xx.status, to_char(xx.event_timestamp, 'DD-MM-YYYY HH24:MI:SS') AS lastChangeTimeDt, \r\n"
			+ "       CASE WHEN xx.status = 'Open' THEN \r\n"
			+ "                 EXTRACT(DAY FROM(nvl(xx.post_event_timestamp, CURRENT_TIMESTAMP) - xx.event_timestamp)) || ' Days : ' ||\r\n"
			+ "				 EXTRACT(HOUR FROM (nvl(xx.post_event_timestamp, CURRENT_TIMESTAMP) - xx.event_timestamp)) || ' Hour : ' ||\r\n"
			+ "				 EXTRACT(MINUTE FROM (nvl(xx.post_event_timestamp, CURRENT_TIMESTAMP) - xx.event_timestamp)) || ' Minute : ' ||\r\n"
			+ "				 FLOOR(EXTRACT(SECOND FROM (nvl(xx.post_event_timestamp, CURRENT_TIMESTAMP) - xx.event_timestamp))) || ' Second'\r\n"
			+ "	   ELSE '' END AS duration,\r\n"
			+ "       CASE WHEN xx.post_event_timestamp IS NOT NULL AND xx.status = 'Open' THEN 'This Event has been Closed.'\r\n"
			+ "            ELSE '' END AS lastChangeReasonDesc, \r\n"
			+ "       xx.tag_status tagStatus, xx.rtu_err_flag rtuErrFlag\r\n"
			+ "  FROM (\r\n"
			+ "SELECT EVENT_OBJ_GROUP_NAME substation_code, EVENT_OBJ_NAME0 feeder_code,\r\n"
			+ "       CASE WHEN CUR_STATUS = 0 AND EVENT_TYPE = '2008' THEN 'Open' \r\n"
			+ "            WHEN CUR_STATUS = 0 AND EVENT_TYPE = '2042' THEN 'Error' \r\n"
			+ "            WHEN CUR_STATUS = 1 AND EVENT_TYPE = '2008' THEN 'Close'  \r\n"
			+ "            WHEN CUR_STATUS = 1 AND EVENT_TYPE = '2042' THEN 'Normal'  \r\n"
			+ "            ELSE 'Error' END AS status,\r\n"
			+ "	   TO_TIMESTAMP(YYMMDD || LPAD(HHMMSSMS, 9, '0'), 'YYYYMMDDHH24MISSFF3') AS event_timestamp,\r\n"
			+ "	   (SELECT min(TO_TIMESTAMP(bb.YYMMDD || LPAD(bb.HHMMSSMS, 9, '0') , 'YYYYMMDDHH24MISSFF3') )\r\n"
			+ "	     FROM HIS_EVENT_TAB bb\r\n"
			+ "	    WHERE aa.EVENT_OBJ_GROUP_NAME = bb.EVENT_OBJ_GROUP_NAME\r\n"
			+ "	      AND aa.EVENT_OBJ_NAME0 = bb.EVENT_OBJ_NAME0\r\n"
			+ "	      AND bb.CUR_STATUS = 1 AND bb.EVENT_TYPE = '2008'\r\n"
			+ "	      AND aa.CUR_STATUS = 0 AND aa.EVENT_TYPE = '2008'\r\n"
			+ "	      AND ((bb.YYMMDD = aa.YYMMDD AND bb.HHMMSSMS > aa.HHMMSSMS ) OR (bb.YYMMDD > aa.YYMMDD ))\r\n"
			+ "	      AND bb.YYMMDD <= to_char(SYSDATE, 'YYYYMMDD')\r\n"
			+ "	      ) AS post_event_timestamp,\r\n"
			+ "       YYMMDD, HHMMSSMS, NULL last_change_reason_desc,\r\n"
			+ "       NULL tag_status, 'Dissabled' as rtu_err_flag\r\n"
			+ "  FROM HIS_EVENT_TAB aa\r\n"
			+ "WHERE CUR_FLOAT_VALUE7 ='300' \r\n"
			+ "AND EVENT_TYPE IN ('2008', '2042')\r\n"
			+ ") xx, TS_PARAM_TAB aa, SUBSTATION_TAB bb\r\n"
			+ "where trim(xx.feeder_code) = trim(aa.NAME) \r\n"
			+ "  AND trim(xx.substation_code)  = trim(bb.NAME)\r\n"
			+ "  AND (:substation = 'ALL' OR bb.NAME = :substation)\r\n"
			+ "  AND (:braker = 'ALL' OR aa.NAME = :braker)\r\n"
			+ "  AND (:status = 'ALL' OR (xx.status = :status AND :status = 'Open' AND xx.post_event_timestamp IS null) \r\n"
			+ "                       OR (xx.status = :status AND xx.status <> 'Open'))\r\n"
			+ "  AND (:changeReason = 'ALL' OR xx.last_change_reason_desc = :changeReason)\r\n"
			+ "  AND (:tagStatus = 'ALL' OR xx.tag_status = :tagStatus)\r\n"
			+ "  AND (:rtuErrorFlag = 'ALL' OR xx.rtu_err_flag = :rtuErrorFlag)\r\n"
			+ "  AND xx.YYMMDD <= to_char(SYSDATE, 'YYYYMMDD')\r\n"
			+ "  AND xx.YYMMDD BETWEEN TO_CHAR(TO_DATE(:startDate, 'YYYY-MM-DD'), 'YYYYMMDD') AND TO_CHAR(TO_DATE(:endDate, 'YYYY-MM-DD'), 'YYYYMMDD')\r\n"
			+ "ORDER BY xx.event_timestamp desc", nativeQuery = true)
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
	
	@Query(value = "SELECT count(DISTINCT xx.feeder_code) Total_feeder,\r\n"
			+ "       sum(CASE WHEN (xx.status = 'Open' AND xx.post_event_timestamp IS NULL) THEN 1 ELSE 0 end) AS open_feeder,\r\n"
			+ "       (count(DISTINCT xx.feeder_code) - sum(CASE WHEN (xx.status = 'Open' AND xx.post_event_timestamp IS NULL) THEN 1 ELSE 0 end)) AS close_feeder,\r\n"
			+ "       '0' AS Error_feeder\r\n"
			+ "  FROM (\r\n"
			+ "SELECT EVENT_OBJ_GROUP_NAME substation_code, EVENT_OBJ_NAME0 feeder_code,\r\n"
			+ "       CASE WHEN CUR_STATUS = 0 AND EVENT_TYPE = '2008' THEN 'Open' \r\n"
			+ "            WHEN CUR_STATUS = 0 AND EVENT_TYPE = '2042' THEN 'Error' \r\n"
			+ "            WHEN CUR_STATUS = 1 AND EVENT_TYPE = '2008' THEN 'Close'  \r\n"
			+ "            WHEN CUR_STATUS = 1 AND EVENT_TYPE = '2042' THEN 'Normal'  \r\n"
			+ "            ELSE 'Error' END AS status,\r\n"
			+ "	   TO_TIMESTAMP(YYMMDD || LPAD(HHMMSSMS, 9, '0'), 'YYYYMMDDHH24MISSFF3') AS event_timestamp,\r\n"
			+ "	   (SELECT min(TO_TIMESTAMP(bb.YYMMDD || LPAD(bb.HHMMSSMS, 9, '0') , 'YYYYMMDDHH24MISSFF3') )\r\n"
			+ "	     FROM HIS_EVENT_TAB bb\r\n"
			+ "	    WHERE aa.EVENT_OBJ_GROUP_NAME = bb.EVENT_OBJ_GROUP_NAME\r\n"
			+ "	      AND aa.EVENT_OBJ_NAME0 = bb.EVENT_OBJ_NAME0\r\n"
			+ "	      AND bb.CUR_STATUS = 1 AND bb.EVENT_TYPE = '2008'\r\n"
			+ "	      AND aa.CUR_STATUS = 0 AND aa.EVENT_TYPE = '2008'\r\n"
			+ "	      AND ((bb.YYMMDD = aa.YYMMDD AND bb.HHMMSSMS > aa.HHMMSSMS ) OR (bb.YYMMDD > aa.YYMMDD ))\r\n"
			+ "	      AND bb.YYMMDD <= to_char(SYSDATE, 'YYYYMMDD')\r\n"
			+ "	      ) AS post_event_timestamp,\r\n"
			+ "       YYMMDD, HHMMSSMS, NULL last_change_reason_desc,\r\n"
			+ "       NULL tag_status, 'Dissabled' as rtu_err_flag\r\n"
			+ "  FROM HIS_EVENT_TAB aa\r\n"
			+ "WHERE CUR_FLOAT_VALUE7 ='300' \r\n"
			+ "AND EVENT_TYPE IN ('2008', '2042')\r\n"
			+ ") xx, TS_PARAM_TAB aa, SUBSTATION_TAB bb\r\n"
			+ "where trim(xx.feeder_code) = trim(aa.NAME) \r\n"
			+ "  AND trim(xx.substation_code)  = trim(bb.NAME)\r\n"
			+ "  AND (:substation = 'ALL' OR bb.NAME = :substation)\r\n"
			+ "  AND (:braker = 'ALL' OR aa.NAME = :braker)\r\n"
			+ "  AND (:status = 'ALL' OR (xx.status = :status AND :status = 'Open' AND xx.post_event_timestamp IS null) \r\n"
			+ "                       OR (xx.status = :status AND xx.status <> 'Open'))\r\n"
			+ "  AND (:changeReason = 'ALL' OR xx.last_change_reason_desc = :changeReason)\r\n"
			+ "  AND (:tagStatus = 'ALL' OR xx.tag_status = :tagStatus)\r\n"
			+ "  AND (:rtuErrorFlag = 'ALL' OR xx.rtu_err_flag = :rtuErrorFlag)\r\n"
			+ "  AND xx.YYMMDD <= to_char(SYSDATE, 'YYYYMMDD')\r\n"
			+ "  AND xx.YYMMDD BETWEEN TO_CHAR(TO_DATE(:startDate, 'YYYY-MM-DD'), 'YYYYMMDD') AND TO_CHAR(TO_DATE(:endDate, 'YYYY-MM-DD'), 'YYYYMMDD')", nativeQuery = true)
	List<FeederSummaryI> getFeederSummary(String substation, String braker, String status, String changeReason, String tagStatus, String rtuErrorFlag,
			String startDate, String endDate);

	Optional<Users> findByUsername(String username);
}
