# Spring Session Authentication Guide

## ✅ Frontend Updated for Spring Session (Cookie-Based)

Your frontend is now configured to work with **Spring Session authentication** using cookies (JSESSIONID), not JWT tokens.

---

## 🔐 How Session Authentication Works

### Authentication Flow

```
1. User logs in → Backend validates credentials
2. Backend creates session and sends JSESSIONID cookie
3. Browser automatically stores cookie
4. All subsequent requests include cookie automatically
5. Backend validates session on each request
```

---

## 🚀 Key Changes Made

### 1. **Removed JWT Token Logic**
- ❌ No `Authorization: Bearer {token}` header
- ❌ No manual token storage
- ✅ Using `credentials: 'include'` instead

### 2. **Added Cookie Support**
All API calls now include `credentials: 'include'`:

```javascript
// Before (JWT)
fetch(url, {
  headers: { 'Authorization': 'Bearer ' + token }
});

// Now (Session Cookie)
fetch(url, {
  credentials: 'include'  // ← Automatically sends cookies
});
```

### 3. **Login Process**
```javascript
const response = await fetch('http://localhost:8082/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  credentials: 'include',  // ← Receive session cookie
  body: JSON.stringify({ email, password })
});

// Backend sets: Set-Cookie: JSESSIONID=xyz...
// Browser automatically stores cookie
```

### 4. **Logout Process**
```javascript
async function logout() {
  // Call backend to invalidate session
  await fetch('http://localhost:8082/api/auth/logout', {
    method: 'POST',
    credentials: 'include'  // ← Send session cookie to be invalidated
  });
  
  sessionStorage.clear();
  window.location.href = 'login.html';
}
```

---

## 🛡️ Backend Requirements (Spring Boot)

### 1. **Spring Security Configuration**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()  // Disable CSRF for testing (enable in production!)
            .cors().and()
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                
                // Admin-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .formLogin().disable()  // Disable default form login
            .httpBasic().disable()  // Disable basic auth
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);  // ← Create sessions
        
        return http.build();
    }
}
```

### 2. **CORS Configuration (IMPORTANT!)**

You **MUST** enable credentials in CORS:

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow your frontend origin
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500",
            "http://127.0.0.1:5500"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ← CRITICAL: Must be true for cookies to work
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 3. **Login Controller**

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, 
                                               HttpServletRequest httpRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get user details
            User user = (User) authentication.getPrincipal();
            
            // Create session (Spring does this automatically)
            HttpSession session = httpRequest.getSession(true);
            
            // Return user info (NO TOKEN!)
            LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .role(user.getRole().name())  // "ADMIN" or "USER"
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "User registered successfully"));
    }
}
```

**Login Response (NO TOKEN):**
```json
{
  "userId": 1,
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "role": "ADMIN"
}
```

### 4. **Session Configuration (application.properties)**

```properties
# Session timeout (30 minutes)
server.servlet.session.timeout=30m

# Cookie settings
server.servlet.session.cookie.name=JSESSIONID
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false  # Set true in production with HTTPS
server.servlet.session.cookie.same-site=lax
```

---

## 📋 Testing Your Backend

### Test Login with cURL

```bash
# Login and save cookie
curl -i -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin123"}' \
  -c cookies.txt

# Response should include:
# Set-Cookie: JSESSIONID=ABC123...

# Use cookie in subsequent request
curl -X GET http://localhost:8082/api/posts \
  -b cookies.txt
```

### Test with Browser DevTools

1. Open browser DevTools (F12)
2. Go to **Application** → **Cookies**
3. After login, you should see: `JSESSIONID` cookie
4. All requests should automatically include this cookie

---

## 🔍 How to Verify It's Working

### Frontend (Browser Console)

```javascript
// Check session storage (user info only, NO token)
console.log(sessionStorage.getItem('userName'));    // Should have value
console.log(sessionStorage.getItem('userRole'));    // Should have value
console.log(sessionStorage.getItem('token'));       // Should be NULL

// Check cookies
document.cookie  // Should contain JSESSIONID
```

### Backend Logs

Add logging to see session creation:

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, 
                                           HttpServletRequest httpRequest) {
    // ... authentication code ...
    
    HttpSession session = httpRequest.getSession(true);
    System.out.println("Session ID created: " + session.getId());
    
    // ... rest of code ...
}
```

---

## 🐛 Common Issues & Solutions

### Issue: "CORS Error - credentials not allowed"

**Problem:** CORS not configured for credentials

**Solution:** 
```java
configuration.setAllowCredentials(true);  // ← Add this
```

### Issue: "Cookie not being sent"

**Solutions:**
1. Check `credentials: 'include'` in all fetch calls ✅
2. Verify CORS `allowCredentials` is true ✅
3. Check cookie domain matches (localhost = localhost)
4. Ensure frontend and backend are on same domain or proper CORS setup

### Issue: "Session not found / 401 error"

**Solutions:**
1. Session expired (default 30 min)
2. Backend not creating session on login
3. Cookie was deleted/blocked
4. Frontend not sending credentials

### Issue: "Login works but subsequent requests fail"

**Problem:** Probably missing `credentials: 'include'`

**Solution:** All fetch calls must include:
```javascript
fetch(url, {
  credentials: 'include'  // ← Don't forget this!
});
```

---

## 📊 Session vs JWT Comparison

| Feature | JWT (Before) | Session (Now) |
|---------|-------------|---------------|
| Storage | Token in sessionStorage | Cookie (JSESSIONID) |
| Auth Header | `Authorization: Bearer {token}` | None (cookie sent automatically) |
| Backend | Stateless | Stateful (stores sessions) |
| Fetch Option | `headers: {...}` | `credentials: 'include'` |
| Logout | Frontend only | Backend invalidates session |
| Security | Token can be stolen | HttpOnly cookies safer |

---

## ✅ What's Stored Where

### Frontend (sessionStorage)
```javascript
sessionStorage.getItem('userId')      // User's ID
sessionStorage.getItem('userName')    // User's name
sessionStorage.getItem('userEmail')   // User's email
sessionStorage.getItem('userRole')    // ADMIN or USER
```

### Browser (Cookies)
```
JSESSIONID=ABC123...  // ← Session ID (sent automatically)
```

### Backend (Server Memory/Database)
```
Session Store:
  JSESSIONID → { userId: 1, username: "admin", role: "ADMIN", ... }
```

---

## 🎯 Quick Checklist

### Backend:
- [ ] CORS configured with `allowCredentials: true`
- [ ] Session management enabled (not STATELESS)
- [ ] Login creates session and sets authentication
- [ ] Logout invalidates session
- [ ] Login response returns user info (NO TOKEN)

### Frontend:
- [ ] All fetch calls use `credentials: 'include'`
- [ ] Login stores user info (NOT token)
- [ ] Logout calls backend endpoint
- [ ] No Authorization header being sent
- [ ] Cookies visible in DevTools

---

## 📞 Dependencies

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- NO JWT dependencies needed! -->
</dependencies>
```

---

**Your frontend is now ready for Spring Session authentication! 🎉**

The key difference: **Cookies instead of JWT tokens** - simpler and more secure!

