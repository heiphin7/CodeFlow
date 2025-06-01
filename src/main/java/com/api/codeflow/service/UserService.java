package com.api.codeflow.service;

import com.api.codeflow.dto.*;
import com.api.codeflow.exception.EmailIsTakenException;
import com.api.codeflow.exception.UsernameIsTakenException;
import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.*;
import com.api.codeflow.repository.RoleRepository;
import com.api.codeflow.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    public void register(RegisterDto dto) throws UsernameIsTakenException,
                                                 EmailIsTakenException,
                                                 IllegalArgumentException {
        if (dto.getUsername().length() < 4 || dto.getUsername().length() > 20) {
            throw new IllegalArgumentException("Username must be between 4 and 20 characters");
        }

        if (dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        String email = dto.getEmail();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (email == null || !email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailIsTakenException("Email is already taken!");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameIsTakenException("Username is already taken!");
        }

        Role role = roleRepository.findByName("ROLE_USER");

        // If everything is OK, create & save new User
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());

        // encode password
        user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(
                role // role_user
        ));

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public AuthResponse login(AuthDto dto) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtTokenUtils.generateAccessToken(userDetails);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUsername(dto.getUsername());
        authResponse.setRoles(
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );

        return authResponse;
    }

    public UpdatedInfoDto updateUserInfo(UpdateUserInfoDto dto, HttpServletRequest request) {
        String username = jwtTokenUtils.getUsernameFromRequest(request);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("User with username=" + username + " not founded!")
        );

        if (!user.getUsername().equals(dto.getUsername())) {
            User checkNewUsername = userRepository.findByUsername(dto.getUsername()).orElse(null);

            if (checkNewUsername != null) {
                throw new IllegalArgumentException("New username is taken!");
            }
        }

        if (!user.getEmail().equals(dto.getEmail())) {
            User checkEmail = userRepository.findByEmail(dto.getEmail()).orElse(null);

            if (checkEmail != null) {
                throw new IllegalArgumentException("New email is taken!");
            }
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setLocation(dto.getLocation());
        user.setGithubLink(dto.getGithubLink());

        userRepository.save(user);

        UpdatedInfoDto response = new UpdatedInfoDto();
        response.setNewUsername(dto.getUsername());
        response.setToken(jwtTokenUtils.generateAccessToken(
                userDetailsService.loadUserByUsername(dto.getUsername())
        ));

        return response;
    }

    @Transactional
    public UserInfoDto getUserInfo(String username) {
        User user = userRepository.findByUsernameWithSubmissions(username).orElseThrow(
                () -> new IllegalArgumentException("User not found!")
        );

        UserInfoDto dto = new UserInfoDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPrimaryLanguage(user.getPrimaryLanguage());
        dto.setLocation(user.getLocation());
        dto.setJoinedDate(user.getJoinedDate());
        dto.setSuccessRate(calculateSuccessRate(user));
        dto.setGithubLink(user.getGithubLink());
        dto.setAverageDifficulty(getAverageDifficulty(user));
        dto.setLastSeen(user.getLastSeen());
        dto.setThisMonthSubmissions(getThisMonthSubmissions(user));
        dto.setThisWeekSolvedProblems(getThisWeekSolvedProblems(user));
        dto.setPreferredLanguage(user.getPreferredLanguage());

        Map<String, ? extends Serializable> peakDayInfo = getPeakSolvedDay(user);
        dto.setPeakDay((String) peakDayInfo.get("peakDay"));
        dto.setPeakDaySolvedProblems((Integer) peakDayInfo.get("peakDaySolvedProblems"));

        return dto;
    }

    private String getAverageDifficulty(User user) {
        Set<Submission> submissions = user.getSubmissions();

        if (submissions == null || submissions.isEmpty()) {
            return "Unknown";
        }

        // Уникальные решённые задачи
        Set<Task> solvedTasks = submissions.stream()
                .filter(Submission::isSuccess)
                .map(Submission::getTask)
                .collect(Collectors.toSet());

        if (solvedTasks.isEmpty()) {
            return "Unknown";
        }

        // Преобразуем уровни сложности в числа
        List<Integer> difficultyValues = solvedTasks.stream()
                .map(task -> switch (task.getDifficulty().toString()) {
                    case "Easy" -> 1;
                    case "Medium" -> 2;
                    case "Hard" -> 3;
                    default -> 0;
                })
                .filter(val -> val > 0) // отфильтровываем неизвестные
                .toList();

        if (difficultyValues.isEmpty()) {
            return "Unknown";
        }

        double avg = difficultyValues.stream().mapToInt(i -> i).average().orElse(0.0);

        // Переводим обратно в текст
        if (avg < 1.5) return "Easy";
        else if (avg < 2.5) return "Medium";
        else return "Hard";
    }

    private Double calculateSuccessRate(User user) {
        Set<Submission> submissions = user.getSubmissions();

        if (submissions == null || submissions.isEmpty()) {
            log.info("No submissions — successRate is null");
            return null;
        }

        long totalAttempts = submissions.size();
        long successfulAttempts = submissions.stream()
                .filter(Submission::isSuccess)
                .count();

        log.info("SuccessRate for user={}: success={}, total={}",
                user.getUsername(), successfulAttempts, totalAttempts);

        if (totalAttempts == 0) return null;

        double rate = (double) successfulAttempts / totalAttempts;
        return Math.round(rate * 1000.0) / 10.0; // например 78.4
    }


    private int getThisMonthSubmissions(User user) {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.of(now.getYear(), now.getMonth());

        return (int) user.getSubmissions().stream()
                .filter(sub -> {
                    if (sub.getCreatedAt() == null) return false;
                    LocalDate date = sub.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return YearMonth.from(date).equals(currentMonth);
                })
                .count();
    }

    private int getThisWeekSolvedProblems(User user) {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

        return (int) user.getSubmissions().stream()
                .filter(Submission::isSuccess)
                .filter(sub -> {
                    if (sub.getCreatedAt() == null) return false;
                    LocalDate date = sub.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return !date.isBefore(sevenDaysAgo);
                })
                .map(Submission::getTask)
                .distinct()
                .count();
    }

    public Map<String, ? extends Serializable> getPeakSolvedDay(User user) {
        Map<DayOfWeek, Long> solvedPerDay = user.getSubmissions().stream()
                .filter(Submission::isSuccess)
                .filter(sub -> sub.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        sub -> sub.getCreatedAt()
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .getDayOfWeek(),
                        Collectors.mapping(Submission::getTask, Collectors.toSet()) // Учитываем уникальные задачи
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (long) e.getValue().size()
                ));

        return solvedPerDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> Map.of(
                        "peakDay", entry.getKey().toString(),
                        "peakDaySolvedProblems", entry.getValue().intValue()
                ))
                .orElse(Map.of(
                        "peakDay", "N/A",
                        "peakDaySolvedProblems", 0
                ));
    }

    @Transactional
    public List<SolvedTaskDto> getSolvedTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not founded!"));

        return user.getSolvedTasks().stream()
                .map(ts -> {
                    SolvedTaskDto dto = new SolvedTaskDto();
                    dto.setId(ts.getTask().getId());
                    dto.setTaskName(ts.getTask().getTitle());
                    dto.setTimeUsage(ts.getExecutionTime() != null ? ts.getExecutionTime() * 1000 : null); // в миллисекундах
                    dto.setMemoryUsage(ts.getMemoryUsage());
                    dto.setSolvedAt(ts.getSolvedAt());
                    dto.setCode(ts.getCode());
                    dto.setLanguage(ts.getLanguage());
                    return dto;
                })
                .sorted(Comparator.comparing(SolvedTaskDto::getSolvedAt).reversed()) // самые свежие сверху
                .toList();
    }


    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }
}
