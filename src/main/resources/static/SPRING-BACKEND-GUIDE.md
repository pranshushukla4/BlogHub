# Spring Backend Integration Guide

## ✅ Your Frontend is Already Configured for Spring Security + JWT

The frontend is set up to work with your Spring Boot backend using JWT tokens and role-based access control.

---

## 🔐 How Authentication Works

### 1. Login Flow

```
User enters credentials → Frontend sends to /api/auth/login → 
Backend validates & returns JWT token → Frontend stores token → 
Frontend includes token in all requests
```

### 2. What the Frontend Expects from Backend

#### **Login Endpoint: POST `/api/auth/login`**

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "userName": "John Doe",
  "userEmail": "user@example.com",
  "role": "ADMIN"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials"
}
```

---

#### **Register Endpoint: POST `/api/auth/register`**

**Request:**
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "about": "Optional description"
}
```

**Expected Response (201 Created):**
```json
{
  "message": "User registered successfully"
}
```

---

### 3. How Frontend Sends Authenticated Requests

Every API request (except login/register) includes the JWT token:

```javascript
// Example from main.js
async function apiGet(url) {
  const token = sessionStorage.getItem('token');
  
  const response = await fetch(url, {
    headers: { 
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token  // ← JWT token here
    }
  });
  
  return await response.json();
}
```

**HTTP Request Example:**
```http
GET /api/posts HTTP/1.1
Host: localhost:8082
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🛡️ Spring Security Configuration

Your backend should have something like this:

### Sample Spring Security Config

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().and()
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Sample JWT Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Validate token and set authentication
            if (jwtService.isTokenValid(token)) {
                String email = jwtService.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Sample Login Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user.getEmail());
            
            LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .role(user.getRole().name())  // "ADMIN" or "USER"
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(null);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Create user logic
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "User registered successfully"));
    }
}
```

---

## 📋 Role-Based Access in Backend

### Using @PreAuthorize

```java
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    // Anyone authenticated can view
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }
    
    // Only ADMIN can create
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Category createCategory(@RequestBody CategoryDto dto) {
        return categoryService.create(dto);
    }
    
    // Only ADMIN can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
```

### Using Method Security

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

---

## 🔧 CORS Configuration

Make sure CORS is enabled for your frontend:

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5500", "http://127.0.0.1:5500"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

---

## 📊 Database: User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password;  // Should be encoded with BCrypt
    
    private String about;
    
    @Enumerated(EnumType.STRING)
    private Role role;  // ADMIN or USER
    
    // getters, setters, constructors
}

public enum Role {
    ADMIN, USER
}
```

---

## ✅ Testing Checklist

### Backend Endpoints to Verify:

- [ ] **POST** `/api/auth/login` - Returns token and user info
- [ ] **POST** `/api/auth/register` - Creates new user
- [ ] **GET** `/api/posts` - Requires auth token
- [ ] **POST** `/api/posts` - Requires auth token
- [ ] **GET** `/api/categories` - Requires auth token
- [ ] **POST** `/api/categories` - Requires ADMIN role
- [ ] **DELETE** `/api/categories/{id}` - Requires ADMIN role
- [ ] **GET** `/api/users` - Requires ADMIN role
- [ ] **DELETE** `/api/users/{id}` - Requires ADMIN role

### Test with Postman/cURL:

```bash
# 1. Login
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin123"}'

# Response: {"token":"eyJ...", "userId":1, "userName":"Admin", "role":"ADMIN"}

# 2. Use token in subsequent requests
curl -X GET http://localhost:8082/api/posts \
  -H "Authorization: Bearer eyJ..."
```

---

## 🐛 Common Issues & Solutions

### Issue: "CORS Error"
**Solution:** Add CORS configuration (see above)

### Issue: "401 Unauthorized"
**Solutions:**
- Check token is being sent in Authorization header
- Verify token is valid and not expired
- Check JWT filter is processing the token

### Issue: "403 Forbidden"
**Solutions:**
- User doesn't have required role (ADMIN)
- Check @PreAuthorize annotations
- Verify role is set correctly in database

### Issue: "Token not found in request"
**Solution:** Ensure frontend is sending `Authorization: Bearer {token}` header

---

## 📦 Required Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
</dependencies>
```

---

## 🎯 Summary

### Frontend (Already Done ✅)
- Sends credentials to `/api/auth/login`
- Stores JWT token in sessionStorage
- Includes token in all API requests as `Authorization: Bearer {token}`
- Handles 401/403 errors by redirecting to login
- Shows/hides UI based on user role

### Backend (Your Part)
- Validate credentials and return JWT token
- Verify token on each request
- Check user roles for protected endpoints
- Return 401 for invalid tokens
- Return 403 for insufficient permissions

---

## 📞 Quick Reference

**Frontend Storage:**
```javascript
sessionStorage.getItem('token')      // JWT token
sessionStorage.getItem('userId')     // User ID
sessionStorage.getItem('userName')   // User name
sessionStorage.getItem('userRole')   // ADMIN or USER
```

**Backend Expects:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Role Names (Must Match):**
- `ADMIN` - Full access
- `USER` - Limited access

---

**Your frontend is ready! Just make sure your Spring backend returns the correct response format. 🚀**

