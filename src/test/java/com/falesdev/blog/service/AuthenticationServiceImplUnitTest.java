package com.falesdev.blog.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplUnitTest {

    /*@Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "password";
    private final String testToken = "jwt.token";
    private final String testRefreshToken = "jwt.refreshToken";
    private final UUID userId = UUID.randomUUID();
    private final org.springframework.security.core.userdetails.User userDetails =
            new org.springframework.security.core.userdetails.User(
                    testEmail,
                    testPassword,
                    new ArrayList<>()
            );

    @Test
    @DisplayName("Authenticate user - Success")
    void authenticate_ValidCredentials_ReturnsToken() {
        User user = User.builder()
                .id(userId)
                .email(testEmail)
                .password(testPassword)
                .build();
        BlogUserDetails userDetails = new BlogUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(testRefreshToken);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(userDetails)).thenReturn(testToken);
        when(refreshTokenService.createRefreshToken(userDetails.getId())).thenReturn(refreshToken);
        when(jwtService.getExpirationTime(testToken)).thenReturn(3600000L);

        AuthResponse response = authenticationService.authenticate(testEmail, testPassword);

        assertThat(response.getToken()).isEqualTo(testToken);
        assertThat(response.getRefreshToken()).isEqualTo(testRefreshToken);
        assertThat(response.getExpiresIn()).isEqualTo(3600000L);
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(testEmail, testPassword)
        );
        verify(userDetailsService).loadUserByUsername(testEmail);
    }

    @Test
    @DisplayName("Register new user - Success")
    void register_NewUser_ReturnsToken() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        Role userRole = Role.builder().name("USER").build();

        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(testPassword)).thenReturn("encodedPassword");

        User expectedUser = User.builder()
                .name("Test User")
                .email(testEmail)
                .password("encodedPassword")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
        BlogUserDetails expectedUserDetails = new BlogUserDetails(expectedUser);
        when(userDetailsService.loadUserByUsername(testEmail)).thenReturn(expectedUserDetails);

        when(jwtService.generateToken(expectedUserDetails)).thenReturn(testToken);
        when(jwtService.getExpirationTime(testToken)).thenReturn(3600L);

        AuthResponse response = authenticationService.register(request);

        assertThat(response.getToken()).isEqualTo(testToken);
        assertThat(response.getExpiresIn()).isEqualTo(3600L);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(testEmail) &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(userRole)
        ));
        verify(roleRepository, times(1)).findByName("USER");
        verify(passwordEncoder).encode(testPassword);
        verify(emailService).sendWelcomeEmail(testEmail, "Test User");
    }

    @Test
    @DisplayName("Register with existing email - Throws Exception")
    void register_ExistingEmail_ThrowsException() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    @DisplayName("Register with missing USER role - Throws Exception")
    void register_MissingUserRole_ThrowsException() {
        SignupRequest request = new SignupRequest("Test User", testEmail, testPassword);
        when(userRepository.existsByEmailIgnoreCase(testEmail)).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role USER not found");
    }

    @Test
    @DisplayName("Generate token - Success")
    void generateToken_ValidUser_ReturnsToken() {
        User userEntity = new User();
        userEntity.setEmail(testEmail);

        BlogUserDetails blogUserDetails = new BlogUserDetails(userEntity);

        when(jwtService.generateToken(eq(blogUserDetails))).thenReturn(testToken);

        String token = authenticationService.generateToken(blogUserDetails);

        verify(jwtService).generateToken(eq(blogUserDetails));
        assertThat(token).isEqualTo(testToken);
    }

    @Test
    @DisplayName("Validate valid token - Success")
    void validateToken_ValidToken_ReturnsUserDetails() {
        Claims claims = mock(Claims.class);
        when(jwtService.parseClaims(eq(testToken))).thenReturn(claims);
        when(claims.getSubject()).thenReturn(testEmail);
        when(userDetailsService.loadUserByUsername(eq(testEmail))).thenReturn(userDetails);

        UserDetails result = authenticationService.validateToken(testToken);

        assertThat(result.getUsername()).isEqualTo(testEmail);
        verify(jwtService).parseClaims(eq(testToken));
        verify(userDetailsService).loadUserByUsername(eq(testEmail));
    }

    @Test
    @DisplayName("Validate invalid token - Throws Exception")
    void validateToken_InvalidToken_ThrowsException() {
        when(jwtService.parseClaims(anyString())).thenThrow(JwtException.class);

        assertThatThrownBy(() -> authenticationService.validateToken("invalid.token"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication error");
    }

    @Test
    @DisplayName("Validate expired token - Throws Exception")
    void validateToken_ExpiredToken_ThrowsException() {
        when(jwtService.parseClaims(testToken)).thenThrow(ExpiredJwtException.class);

        assertThatThrownBy(() -> authenticationService.validateToken(testToken))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    @DisplayName("Get user profile - Success")
    void getUserProfile_ValidAuthentication_ReturnsAuthUser() {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("USER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRoles(roles);

        BlogUserDetails userDetails = new BlogUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AuthUserResponse authUserResponse = authenticationService.getUserProfile(authentication);

        assertThat(authUserResponse.getId()).isEqualTo(user.getId());
        assertThat(authUserResponse.getName()).isEqualTo("Test User");
        assertThat(authUserResponse.getEmail()).isEqualTo("test@example.com");
        Set<RoleDto> roleDtos = authUserResponse.getRoles();
        assertThat(roleDtos.size()).isEqualTo(1);
        RoleDto roleDto = roleDtos.iterator().next();
        assertThat(roleDto.getId()).isEqualTo(role.getId());
        assertThat(roleDto.getName()).isEqualTo("USER");
        verify(authentication).getPrincipal();
    }*/
}
