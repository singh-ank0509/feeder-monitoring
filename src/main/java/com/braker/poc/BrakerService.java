package com.braker.poc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BrakerService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	BrakerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository=userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public ResponseEntity<?> getBrakerData(String substation, String braker, String status, String changeReason, String tagStatus, String rtuErrorFlag, String startDate, String endDate) 
	{
		List<BrakerI> brakerData = userRepository.getBrakerData(substation, braker, status, changeReason, tagStatus, rtuErrorFlag, startDate, endDate);
		return ResponseEntity.ok(brakerData);
	}
	
	public String registerUser(UserDto userDto) {
		Users user = new Users();
		user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return "User saved successfully!!!";
    }

	public Object login(UserDto dto) {
//		Users user = userRepository.findByUsername(dto.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));

		Users user = new Users();
		user.setUsername("admin");
		user.setPassword("$2a$16$NUAN1FDOLyTH48FqMxEULe6R2V7AyToLsh1v2s266xxfOy3ZV3Sc6");
        if (!checkPassword(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return Map.of("message", "Login successful");
	}
	
	public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

	public ResponseEntity<?> getSubstation() {
		List<BrakerI> substations = userRepository.getSubstation();
		return ResponseEntity.ok(substations);
	}
	
	public ResponseEntity<?> getFeeders(String substation) {
		List<BrakerI> feeders = userRepository.getFeeder(substation);
		return ResponseEntity.ok(feeders);
	}

	public ResponseEntity<?> getFeederSummary(String substation, String braker, String status, String changeReason,
			String tagStatus, String rtuErrorFlag, String startDate, String endDate) 
	{
		List<FeederSummaryI> feederSummary = userRepository.getFeederSummary(substation, braker, status, changeReason, tagStatus, rtuErrorFlag, startDate, endDate);
		return ResponseEntity.ok(feederSummary);
	}
}

