# BrailleMate 🔵⠿

**점역교정사와 점자 학습자를 위한 AI 점자 컨설팅 앱**

2024 개정 한국 점자 규정(문화체육관광부고시 제2024-0005호)을 기반으로 정확한 점자 정보와 AI 기반 컨설팅을 제공합니다.

## 주요 기능

### 📖 점자 규정 레퍼런스 (오프라인)
- 2024 한국 점자 규정 전문 265개 조항 수록
- 한글, 수학, 과학, 한국 음악, 서양 음악 점자 전 범위
- 전문검색(FTS) 지원
- 북마크 기능

### 🤖 AI 점자 컨설팅 (온라인)
- Google Gemini API 기반 대화형 컨설팅
- 규정 조항 자동 인용
- 점자 표기 예시 제공
- 대화 기록 저장

### ♿ 접근성
- TalkBack 완벽 지원
- 큰 터치 타겟 (48dp+)
- 고대비 모드

## 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Kotlin 2.1 |
| UI | Jetpack Compose + Material Design 3 |
| 아키텍처 | MVVM + Clean Architecture |
| 로컬 DB | Room + FTS4 |
| AI | Google Gemini API |
| DI | Hilt |
| 패키지 | `kr.ac.kaist.aailab.braillemate.android` |

## 빌드 방법

### 요구 사항
- Android Studio Ladybug (2024.2+)
- JDK 17
- Android SDK 35

### 빌드
```bash
# 규정 데이터 파싱 (최초 1회)
python3 parse_regulations.py

# Android Studio에서 열거나 CLI 빌드
./gradlew assembleDebug
```

### API 키 설정
1. [Google AI Studio](https://aistudio.google.com/apikey)에서 Gemini API 키 발급
2. 앱 설정 > API 키 입력

## 라이선스

점자 규정 데이터: 문화체육관광부 (공공저작물)

---

**KAIST AAI Lab** | 2024 개정 한국 점자 규정 기반
