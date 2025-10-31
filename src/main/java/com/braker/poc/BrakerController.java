package com.braker.poc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BrakerController {
	
	private final BrakerService brakerService;

	BrakerController(BrakerService brakerService){
		this.brakerService=brakerService;
	}
	
	@GetMapping("/braker-data")
	public ResponseEntity<?> getBrakerData(@RequestParam(defaultValue = "ALL") String substation,
										@RequestParam(defaultValue = "ALL") String braker,
										@RequestParam(defaultValue = "ALL") String status,
										@RequestParam(defaultValue = "ALL") String changeReason,
										@RequestParam(defaultValue = "ALL") String tagStatus,
										@RequestParam(defaultValue = "ALL") String rtuErrorFlag,
										@RequestParam String startDate,
										@RequestParam String endDate) 
	{		
		return brakerService.getBrakerData(substation, braker, status, changeReason, tagStatus, rtuErrorFlag, startDate, endDate);
	}
	
	@GetMapping("/substation")
	public ResponseEntity<?> getSubstation(@RequestParam(defaultValue = "ALL") String substation) 
	{		
		return brakerService.getSubstation(substation);
	}
	
	@PostMapping("/register-user")
	public ResponseEntity<?> registerUser(@RequestBody UserDto dto) 
	{		
		return ResponseEntity.ok(brakerService.registerUser(dto));
	}
	
	@GetMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDto dto)
	{		
		try {
			return ResponseEntity.ok(brakerService.login(dto));
		} catch(Exception ex) {
			ex.printStackTrace();
        	return new ResponseEntity<String>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
		}
	}
}
