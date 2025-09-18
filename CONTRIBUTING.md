# Contributing to Tune Tide 🎵

Thank you for your interest in contributing to **Tune Tide**!  
We welcome contributions during Hacktoberfest and beyond 🚀  

---

## 🛠 Project Setup

1. Fork the repository.  
2. Clone your fork locally:  
   ```bash
   git clone https://github.com/<your-username>/TuneTide.git
   cd TuneTide
3. Checkout the develop branch:
   ```bash
   git checkout develop

4. Open the project in Android Studio.
5. Connect a physical Android device with Developer options → USB debugging enabled, then run the app.

## 🌿 Branching Convention
-  All contributions must branch out from develop.

1. Use these conventions for branch names:
    a. Bug fixes → bug/B1_${issueId}
    b. Enhancements → feature/I1_${issueId}
   Example:
   ```bash
      git checkout -b bug/B1_102 develop

**📝 Commit Messages**
  1. Follow a clear format:
  2. bug/enhancementId_${issueId}
     Example:
     ```
       B1_102 Fix album art not loading 
       I1_205 Add search bar in song list 

**✅ Pull Requests**
1. Open a PR against develop (never master).

2. Reference the related issue in the PR description.
   Example:
   ```
    Fixes #102

   
- PRs will be reviewed before merging. Please be patient! 🙌

- Only maintainers can merge PRs into develop or master.
