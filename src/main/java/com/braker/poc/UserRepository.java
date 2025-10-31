package com.braker.poc;

import java.util.List;
import java.util.Optional;

import org.hibernate.query.NativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Long> {
	
	@Query(value = "SELECT bb.NAME substationCode, bb.DESCRIPTION substation, aa.NAME feederCode, aa.DESCRIPTION feeder,\n"
			+ "          xx.status, xx.last_change_time_dt AS lastChangeTimeDt, xx.Duration,\n"
			+ "          xx.last_change_reason_desc AS lastChangeReasonDesc,\n"
			+ "          xx.tag_status AS tagStatus, xx.rtu_err_flag AS rtuErrFlag\n"
			+ "FROM (SELECT name,\n"
			+ "			CASE\n"
			+ "			    WHEN double_value = 1 THEN 'Open'\n"
			+ "			    WHEN double_value = 2 THEN 'Close'\n"
			+ "			    ELSE 'Error'\n"
			+ "			END AS status,\n"
			+ "			TO_CHAR(\n"
			+ "			    (TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')),\n"
			+ "			    'DD-MM-YYYY HH24:MI:SS'\n"
			+ "			) AS last_change_time_dt,\n"
			+ "			(EXTRACT(DAY FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Days : '||\n"
			+ "			   EXTRACT(HOUR FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND')))) || ' Hour : '||\n"
			+ "			   (EXTRACT(MINUTE FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Minute : '||\n"
			+ "			   round(EXTRACT(SECOND FROM (SYSTIMESTAMP - (TIMESTAMP '1970-01-01 00:00:00'\n"
			+ "			 + NUMTODSINTERVAL(last_change_time, 'SECOND'))))) || ' Second' AS Duration,\n"
			+ "			last_change_reason_desc,\n"
			+ "			CASE\n"
			+ "			    WHEN tag_flag = 1 THEN 'Enabled'\n"
			+ "			    ELSE 'Disabled'\n"
			+ "			END AS tag_status,\n"
			+ "			rtu_err_flag,\n"
			+ "			(TIMESTAMP '1970-01-01 00:00:00' + NUMTODSINTERVAL(last_change_time, 'SECOND')) last_change_dt\n"
			+ "			FROM aduser.braker_data) xx, aduser.TS_PARAM_TAB aa, aduser.SUBSTATION_TAB bb\n"
			+ "WHERE (:substation = 'ALL' OR bb.NAME = :substation)\n"
			+ "AND (:braker = 'ALL' OR aa.NAME = :braker)\n"
			+ "AND (:status = 'ALL' OR xx.status = :status)\n"
			+ "AND (:changeReason = 'ALL' OR xx.last_change_reason_desc = :changeReason)\n"
			+ "AND (:tagStatus = 'ALL' OR xx.tag_status = :tagStatus)\n"
			+ "AND (:rtuErrorFlag = 'ALL' OR xx.rtu_err_flag = :rtuErrorFlag)\n"
			+ "AND TRUNC(xx.last_change_dt) BETWEEN TO_DATE(:startDate, 'YYYY-MM-DD') AND TO_DATE(:endDate, 'YYYY-MM-DD')\n"
			+ "AND trim(xx.name) = aa.NAME\n"
			+ "AND aa.OF_SUBSTATION  = bb.NAME\n"
			+ "AND aa.\"TYPE\"  = 300\n"
			+ "AND aa.RTU_NO <> -1\n"
			+ "AND aa.TS_NO <> -1\n"
			+ "ORDER BY last_change_dt DESC", nativeQuery = true)
	List<BrakerI> getBrakerData(String substation, String braker, String status, String changeReason, String tagStatus, String rtuErrorFlag,
			String startDate, String endDate);
	
	@Query(value = "SELECT bb.NAME substation_code, bb.DESCRIPTION substation, aa.NAME feeder_code, aa.DESCRIPTION feeder\n"
			+ "FROM aduser.TS_PARAM_TAB aa, aduser.SUBSTATION_TAB bb\n"
			+ "WHERE aa.OF_SUBSTATION  = bb.NAME\n"
			+ "AND (:substation = 'ALL' OR bb.NAME = :substation)\n"
			+ "AND aa.\"TYPE\"  = 300\n"
			+ "AND aa.RTU_NO <> -1\n"
			+ "AND aa.TS_NO <> -1\n"
			+ "ORDER BY bb.DESCRIPTION, aa.DESCRIPTION", nativeQuery = true)
	List<BrakerI> getSubstation(String substation);

	Optional<Users> findByUsername(String username);
}
