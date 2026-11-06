#!/usr/bin/env bash
#
# iOS 壳工程的一键构建。
#
#   bash apps/demo/ios/build.sh                  # 全流程：编 framework → 打 xcframework → 生成 Xcode 工程 → xcodebuild
#   bash apps/demo/ios/build.sh --framework-only # 只重编 Kotlin 产物（Xcode 的脚本阶段会调它）
#
# 只能在 macOS 上跑：Kotlin/Native 的 iOS 目标在 Windows/Linux 上编不出来。
#
# 为什么要 xcframework：Gradle 每个架构只出一个 .framework（真机 arm64、模拟器 arm64、模拟器 x64），
# Xcode 一次构建只会用其中一个。打成 .xcframework 之后 Xcode 自己按「真机/模拟器」挑切片。
set -euo pipefail

IOS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$IOS_DIR/../../.." && pwd)"

FRAMEWORK_ONLY=0
if [[ "${1:-}" == "--framework-only" ]]; then
  FRAMEWORK_ONLY=1
fi

cd "$ROOT"

# Xcode 的构建环境 PATH 很干净，brew 装的 java 不在里面 —— Gradle 会找不到 java
if [[ -z "${JAVA_HOME:-}" ]] && [[ -x /usr/libexec/java_home ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home)"
fi
if [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

KIT_BIN="$ROOT/kit/build/bin"
XC="$IOS_DIR/Frameworks/Kit.xcframework"

echo "==> [1/4] 编译 Kotlin 的 iOS framework（真机 arm64 / 模拟器 arm64 / 模拟器 x64）"
"$ROOT/gradlew" \
  :kit:linkDebugFrameworkIosArm64 \
  :kit:linkDebugFrameworkIosSimulatorArm64 \
  :kit:linkDebugFrameworkIosX64 \
  --no-daemon

echo "==> [2/4] 打成 .xcframework"
rm -rf "$XC"
mkdir -p "$XC"

# 真机切片
cp -R "$KIT_BIN/iosArm64/debugFramework/Kit.framework" "$XC/ios-arm64"

# 模拟器切片：Apple Silicon 跑 arm64、Intel Mac 跑 x86_64 → 合成一个 fat 的
cp -R "$KIT_BIN/iosSimulatorArm64/debugFramework/Kit.framework" "$XC/ios-arm64_x86_64-simulator"
lipo -create \
  "$KIT_BIN/iosSimulatorArm64/debugFramework/Kit.framework/Kit" \
  "$KIT_BIN/iosX64/debugFramework/Kit.framework/Kit" \
  -output "$XC/ios-arm64_x86_64-simulator/Kit.framework/Kit"

# xcframework 的清单：告诉 Xcode 每个切片对应什么平台/架构
cat > "$XC/Info.plist" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>AvailableLibraries</key>
	<array>
		<dict>
			<key>LibraryIdentifier</key>
			<string>ios-arm64</string>
			<key>LibraryPath</key>
			<string>Kit.framework</string>
			<key>SupportedArchitectures</key>
			<array>
				<string>arm64</string>
			</array>
			<key>SupportedPlatform</key>
			<string>ios</string>
		</dict>
		<dict>
			<key>LibraryIdentifier</key>
			<string>ios-arm64_x86_64-simulator</string>
			<key>LibraryPath</key>
			<string>Kit.framework</string>
			<key>SupportedArchitectures</key>
			<array>
				<string>arm64</string>
				<string>x86_64</string>
			</array>
			<key>SupportedPlatform</key>
			<string>ios</string>
			<key>SupportedPlatformVariant</key>
			<string>simulator</string>
		</dict>
	</array>
	<key>CFBundlePackageType</key>
	<string>XFWK</string>
	<key>XCFrameworkFormatVersion</key>
	<string>1.0</string>
</dict>
</plist>
PLIST

if [[ $FRAMEWORK_ONLY -eq 1 ]]; then
  echo "==> framework 就绪：$XC"
  exit 0
fi

echo "==> [3/4] 生成 Xcode 工程（XcodeGen）"
if ! command -v xcodegen >/dev/null 2>&1; then
  echo ">>> 没装 XcodeGen，先装：brew install xcodegen"
  brew install xcodegen
fi
cd "$IOS_DIR"
xcodegen generate

echo "==> [4/4] 编译 iOS App（模拟器）"
xcodebuild \
  -project DemoApp.xcodeproj \
  -scheme DemoApp \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath "$IOS_DIR/build" \
  CODE_SIGNING_ALLOWED=NO \
  build

echo ""
echo "✅ 编完了。产物：$IOS_DIR/build/Build/Products/Debug-iphonesimulator/DemoApp.app"
echo "   装到模拟器：xcrun simctl install booted \"$IOS_DIR/build/Build/Products/Debug-iphonesimulator/DemoApp.app\""
