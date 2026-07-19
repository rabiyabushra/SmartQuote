# Profile Data Flow Diagram

## Sign-Up Flow
```
┌─────────────────┐
│  Sign-Up Form   │
│                 │
│ • businessName  │
│ • ownerName     │
│ • email         │
│ • phone         │
│ • password      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│     POST /api/auth/sign-up              │
│                                         │
│  1. Create Business entity:             │
│     - businessName, ownerName, email,   │
│       phone, address, gstin, logoUrl    │
│                                         │
│  2. Create User entity:                 │
│     - Linked to Business via businessId │
│     - name, email, phone, passwordHash  │
│                                         │
│  3. Generate session token              │
│                                         │
│  4. Return AuthResponse with:           │
│     ✓ token, userId, businessId         │
│     ✓ name, email, role                 │
│     ✓ businessName, phone, address      │
│     ✓ gstin, logoUrl                    │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Frontend AuthService                   │
│                                         │
│  Stores in localStorage:                │
│  - smartquote_token: "xxx"              │
│  - smartquote_user: {full AuthResponse} │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Profile Component                      │
│                                         │
│  Reads from authService.currentUser():  │
│  • Business Name  ← businessName        │
│  • Owner Name     ← name                │
│  • Email          ← email               │
│  • Contact        ← phone               │
│  • GSTIN          ← gstin               │
│  • Address        ← address             │
│  • Logo           ← logoUrl             │
└─────────────────────────────────────────┘
```

## Sign-In Flow
```
┌─────────────────┐
│  Sign-In Form   │
│                 │
│ • email         │
│ • password      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│     POST /api/auth/sign-in              │
│                                         │
│  1. Validate credentials                │
│  2. Generate and send OTP               │
│  3. Return: { message: "OTP sent" }     │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  OTP Dialog     │
│                 │
│ • Enter OTP     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│     POST /api/auth/verify-otp           │
│                                         │
│  1. Verify OTP                          │
│  2. Fetch User by email                 │
│  3. Fetch Business by businessId        │
│  4. Generate session token              │
│                                         │
│  5. Return AuthResponse with:           │
│     ✓ token, userId, businessId         │
│     ✓ name, email, role                 │
│     ✓ businessName, phone, address      │
│     ✓ gstin, logoUrl                    │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Frontend AuthService                   │
│                                         │
│  Stores in localStorage:                │
│  - smartquote_token: "xxx"              │
│  - smartquote_user: {full AuthResponse} │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Profile Component                      │
│                                         │
│  Reads from authService.currentUser():  │
│  • Business Name  ← businessName        │
│  • Owner Name     ← name                │
│  • Email          ← email               │
│  • Contact        ← phone               │
│  • GSTIN          ← gstin               │
│  • Address        ← address             │
│  • Logo           ← logoUrl             │
└─────────────────────────────────────────┘
```

## Profile Page Load Flow
```
┌─────────────────────────────────────────┐
│  User navigates to /profile             │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  ProfileComponent.ngOnInit()            │
│                                         │
│  Calls loadUserProfile()                │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  authService.currentUser()              │
│                                         │
│  Reads from localStorage:               │
│  JSON.parse(smartquote_user)            │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Returns AuthResponse object:           │
│                                         │
│  {                                      │
│    token: "xxx",                        │
│    userId: "u123",                      │
│    businessId: "b456",                  │
│    name: "John Doe",                    │
│    email: "john@example.com",           │
│    role: "admin",                       │
│    businessName: "Acme Corp",           │
│    phone: "9876543210",                 │
│    address: "123 Main St",              │
│    gstin: "29XXXXX1234X1Z5",            │
│    logoUrl: null                        │
│  }                                      │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Profile component updates display:     │
│                                         │
│  profile.update({                       │
│    businessName: "Acme Corp",           │
│    ownerName: "John Doe",               │
│    email: "john@example.com",           │
│    contactNumber: "9876543210",         │
│    address: "123 Main St",              │
│    gstin: "29XXXXX1234X1Z5",            │
│    logoUrl: null                        │
│  })                                     │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  Profile UI displays all fields         │
│                                         │
│  ✓ Business logo/icon                   │
│  ✓ Business Name (editable)             │
│  ✓ Owner Name (editable)                │
│  ✓ Email (editable)                     │
│  ✓ Contact Number (editable)            │
│  ✓ GSTIN (editable)                     │
│  ✓ Address (editable)                   │
└─────────────────────────────────────────┘
```

## Key Points

### ✅ What Works Now:
1. **Single API Call**: Sign-up/Sign-in returns complete profile data
2. **Instant Profile Load**: No additional API calls needed
3. **Persistent Data**: Stored in localStorage, survives page refresh
4. **All Fields Populated**: Every sign-up field appears in profile

### 🔄 Data Storage:
- **Location**: Browser localStorage
- **Keys**: 
  - `smartquote_token` → Session token
  - `smartquote_user` → Complete AuthResponse JSON

### 📝 Note on Editing:
- Profile component has inline editing UI
- Currently saves only to local state
- **TODO**: Connect to backend API to persist changes to database
