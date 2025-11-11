#!/bin/bash

# iOS 설정 스크립트
# Config.xcconfig 파일이 없을 경우 템플릿에서 생성

CONFIG_DIR="iosApp/Configuration"
CONFIG_FILE="$CONFIG_DIR/Config.xcconfig"
TEMPLATE_FILE="$CONFIG_DIR/Config.xcconfig.template"

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔧 iOS 프로젝트 설정 확인 중..."

# Config.xcconfig 파일 확인 및 생성
if [ ! -f "$CONFIG_FILE" ]; then
    if [ -f "$TEMPLATE_FILE" ]; then
        echo -e "${YELLOW}⚠️  Config.xcconfig 파일이 없습니다. 템플릿에서 생성합니다...${NC}"
        cp "$TEMPLATE_FILE" "$CONFIG_FILE"
        echo -e "${GREEN}✅ Config.xcconfig 파일이 생성되었습니다.${NC}"
        echo -e "${YELLOW}📝 필요한 경우 다음 항목을 수정하세요:${NC}"
        echo "   - TEAM_ID: Apple Developer Team ID"
        echo "   - PRODUCT_BUNDLE_IDENTIFIER: Bundle ID"
        echo "   - API Keys (추가 예정인 경우)"
    else
        echo -e "${RED}❌ 템플릿 파일을 찾을 수 없습니다: $TEMPLATE_FILE${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ Config.xcconfig 파일이 이미 존재합니다.${NC}"
fi

# CocoaPods 확인
if ! command -v pod &> /dev/null; then
    echo -e "${YELLOW}⚠️  CocoaPods가 설치되어 있지 않습니다.${NC}"
    echo "다음 명령어로 설치하세요: sudo gem install cocoapods"
else
    echo -e "${GREEN}✅ CocoaPods가 설치되어 있습니다.${NC}"
    
    # Pod 설치
    echo "📦 CocoaPods 의존성 설치 중..."
    cd iosApp && pod install
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ CocoaPods 의존성이 설치되었습니다.${NC}"
    else
        echo -e "${RED}❌ CocoaPods 설치 중 오류가 발생했습니다.${NC}"
    fi
fi

echo ""
echo "🎉 iOS 프로젝트 설정이 완료되었습니다!"
echo "📱 Xcode에서 iosApp.xcworkspace 파일을 열어 실행하세요."