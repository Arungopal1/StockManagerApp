# 🆓 StockManager - ₹0 Deployment Guide

Complete free deployment using **Supabase (Backend)** + **GitHub (Distribution)**.

---

## 📋 Prerequisites (All Free)

| Tool | Purpose | Cost |
|------|---------|------|
| GitHub Account | Code hosting + Actions + Releases | ₹0 |
| Supabase Account | PostgreSQL + Auth + Realtime + Storage | ₹0 |
| Android Studio | Build APK/AAB | ₹0 |

---

## 🚀 Quick Setup (20 minutes)

### 1. Create Supabase Project
```
1. Go to https://supabase.com → Sign up with GitHub
2. "New Project" → Name: "stockmanager" → Free tier
3. Wait 2 minutes for provisioning
4. Settings → API → Copy:
   - Project URL: https://xxxxx.supabase.co
   - Anon Key: eyJhbGciOiJIUzI1NiIs...
```

### 2. Run Database Schema
```
1. Supabase Dashboard → SQL Editor
2. Copy contents of `supabase_schema.sql` 
3. Paste and click "Run"
4. Verify tables created in Table Editor
```

### 3. Create GitHub Repository
```
1. https://github.com/new → "StockManagerApp"
2. Public repository (required for free Actions)
3. Push your code:
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/StockManagerApp.git
   git push -u origin main
```

### 4. Generate Keystore
```bash
keytool -genkey -v \
  -keystore stockmanager-release.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias stockmanager \
  -storepass YOUR_PASSWORD \
  -keypass YOUR_PASSWORD \
  -dname "CN=Your Name, OU=Mobile Shop, O=StockManager, L=Your City, S=Your State, C=IN"

# Convert to base64 (copy this output)
base64 -w0 stockmanager-release.jks
```

### 5. Add GitHub Secrets
Repo → Settings → Secrets and variables → Actions → New repository secret:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | Output from base64 command above |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `stockmanager` |
| `KEY_PASSWORD` | Your key password |
| `SUPABASE_URL` | `https://your-project.supabase.co` |
| `SUPABASE_ANON_KEY` | Your anon key from Supabase |

### 6. Update Build Config
In `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        buildConfigField "String", "SUPABASE_URL", "\"https://your-project.supabase.co\""
        buildConfigField "String", "SUPABASE_ANON_KEY", "\"your-anon-key\""
    }
}
```

### 7. Trigger First Release
```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will:
1. ✅ Build signed AAB + APK
2. ✅ Generate changelog from commits
3. ✅ Create GitHub Release with downloads

**APK URL:** `https://github.com/YOUR_USERNAME/StockManagerApp/releases/latest`

---

## 📱 User Installation (₹0)

### Option 1: Obtainium (Best - Auto-updates)
1. Install Obtainium from F-Droid or GitHub
2. Add repo: `https://github.com/YOUR_USERNAME/StockManagerApp`
3. Install → Auto-notifies on new releases

### Option 2: Direct APK Link
```
https://github.com/YOUR_USERNAME/StockManagerApp/releases/latest/download/app-release.apk
```

### Option 3: GitHub Pages (Pretty install page)
1. Repo Settings → Pages → Source: "GitHub Actions"
2. Add workflow to deploy install page
3. Access at: `https://YOUR_USERNAME.github.io/StockManagerApp/`

---

## 🏪 Multi-Shop Setup

### Create Shops (Admin)
```sql
-- In Supabase SQL Editor
INSERT INTO shops (name, slug, owner_id) 
VALUES ('Downtown Mobile', 'downtown', 'USER_UUID_FROM_AUTH');

-- Update user with shop_id and role
UPDATE auth.users 
SET raw_user_meta_data = jsonb_set(
    raw_user_meta_data, 
    '{shop_id}', '"SHOP_UUID"'
), raw_user_meta_data = jsonb_set(
    raw_user_meta_data, 
    '{role}', '"owner"'
)
WHERE email = 'owner@downtown.com';
```

### Staff Login
Each shop's staff uses their email/password. App automatically scopes all data to their `shop_id` via RLS policies.

---

## 🔄 Sync Architecture

```
┌─────────────┐     Push (unsynced)      ┌─────────────┐
│   Android   │ ──────────────────────▶  │  Supabase   │
│  (Room DB)  │                          │  (Postgres) │
└─────────────┘ ◀──────────────────────  └─────────────┘
       │                    Pull (since last_sync)         │
       │                     Realtime (WebSocket)          │
       ▼                                                    ▼
  Local Cache                                        Cloud Source
```

**Conflict Resolution:** Server wins (last-write-wins based on `updated_at`)

---

## 📊 Free Tier Limits

| Resource | Limit | Typical Capacity |
|----------|-------|------------------|
| Database | 500 MB | ~50K products + 500K transactions |
| Bandwidth | 2 GB/mo | ~100 active users daily |
| Auth | 50K MAU | Unlimited shops/staff |
| Realtime | 200 concurrent | 200 live connections |
| GitHub Actions | 2000 min/mo | ~50 builds/month |
| GitHub Releases | 2 GB | ~200 APK versions |

**For typical mobile shop:** 500 products, 50 txns/day → **100+ shops fit easily**

---

## 🛠 Maintenance (Zero Cost)

### Daily (Automatic)
- GitHub Actions builds on tag push
- Supabase handles backups, SSL, scaling

### Weekly (Manual)
- Check GitHub Actions for failed builds
- Monitor Supabase dashboard for usage

### Monthly
- Review Supabase usage (Dashboard → Reports)
- Rotate GitHub secrets if needed
- Test restore from Supabase backup

---

## 🔧 Troubleshooting

### Build Fails
```bash
# Check locally first
./gradlew assembleRelease

# Common issues:
# - Keystore password wrong → Check secrets
# - SDK version mismatch → Update build.gradle.kts
# - Out of memory → Add GRADLE_OPTS: "-Xmx2g"
```

### Sync Not Working
1. Check Supabase logs: Dashboard → Logs → API
2. Verify RLS policies: `SELECT * FROM pg_policies WHERE tablename = 'products'`
3. Test API directly:
```bash
curl -H "apikey: ANON_KEY" \
     -H "Authorization: Bearer USER_JWT" \
     "https://PROJECT.supabase.co/rest/v1/products?shop_id=eq.SHOP_UUID"
```

### Users Can't Install
- Ensure repo is **Public** (required for free Actions)
- Check Release has `app-release.apk` asset
- Try direct link: `.../releases/latest/download/app-release.apk`

---

## 🔐 Security Checklist

- [ ] Keystore not committed to git (in `.gitignore`)
- [ ] GitHub Secrets used for all credentials
- [ ] Supabase RLS policies enabled on all tables
- [ ] Anon key only (never service role key in app)
- [ ] HTTPS enforced (Supabase + GitHub Pages default)
- [ ] Regular dependency updates via Dependabot

---

## 📈 Scaling Beyond Free Tier

| When | Upgrade To | Cost |
|------|------------|------|
| >500MB DB | Supabase Pro | $25/mo |
| >2GB bandwidth | Supabase Pro | $25/mo |
| >50K MAU | Supabase Pro | $25/mo |
| Private repos | GitHub Team | $4/user/mo |
| More Actions min | GitHub Team | Included |

**Stay free by:** Archiving old transactions, compressing images, limiting realtime connections.

---

## 📞 Support

- **Supabase Issues:** https://github.com/supabase/supabase/discussions
- **GitHub Actions:** https://github.com/actions/runner-images/issues
- **App Issues:** Your GitHub repo Issues tab

---

## ✅ Verification Checklist

- [ ] Supabase project created
- [ ] Schema applied (15 tables + RLS + Realtime)
- [ ] GitHub repo created (Public)
- [ ] Keystore generated + base64 encoded
- [ ] 4 GitHub secrets added
- [ ] Build config updated with Supabase keys
- [ ] Tag pushed → Release created
- [ ] APK downloads and installs
- [ ] Login works with test credentials
- [ ] Sync works (create product → appears on other device)
- [ ] Multi-shop isolation verified

---

**Total Cost: ₹0 forever** 🎉