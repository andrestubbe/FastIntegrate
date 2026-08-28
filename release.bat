@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ===============================================================
echo   Releasing FastIntegrate to GitHub...
echo ===============================================================

git config user.name "Andre Stubbe"
git config user.email "andrestubbe@proton.me"

git add -A
git commit -m "feat: initial release of FastIntegrate v0.1.0"
gh repo create andrestubbe/FastIntegrate --public --source=. --push
git tag 0.1.0
git push origin 0.1.0
gh release create 0.1.0 --title "FastIntegrate 0.1.0" --notes "Initial release of FastIntegrate: Universal Sidecar EventBus, Webhook router, and FastAIRuntime / FastAIMCP tool binding bridge"

echo ===============================================================
echo   Release 0.1.0 Complete!
echo ===============================================================
