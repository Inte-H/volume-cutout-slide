# Volume Cutout Slide

[English](README.md)

삼성 갤럭시 휴대폰용 LSPosed 모듈입니다. 가로화면에서 카메라 쪽에 뜨는 굿락
**Sound Assistant** 볼륨 패널은 카메라 영역 경계에서 불쑥 나타나고 사라집니다.
이 모듈을 쓰면 패널이 화면 끝 바깥에서 들어오고 화면 끝 바깥으로 나갑니다.
패널이 멈추는 위치는 그대로입니다.

## 필요한 것

- LSPosed API 102 이상
- 굿락 Sound Assistant의 볼륨 패널. 모듈은 이 패널만 고치므로 Sound Assistant가
  없으면 아무 일도 하지 않습니다.

SM-S948N, One UI 8.5(Android 16), LSPosed v2.1.1, Sound Assistant 8.5.00.2에서
확인했습니다.

## 사용법

1. [Releases](https://github.com/Inte-H/volume-cutout-slide/releases)에서 APK를 받아 설치합니다.
2. LSPosed에서 **Volume Cutout Slide**를 켜고 적용 대상에 다음을 체크합니다.
   - **시스템 UI** (모듈이 여기서 동작합니다)
   - 화면이 카메라 영역까지 차는 앱 (예: YouTube)
3. 시스템 UI를 재시작하거나 휴대폰을 다시 켭니다.

삼성의 앱별 카메라 컷아웃 설정에서 카메라 영역까지 채우도록 지정한 앱은 체크하지
않아도 적용됩니다. 가로화면에서 패널과 카메라가 모두 화면 오른쪽에 있을 때만 동작합니다.

LSPosed는 적용 대상이 바뀌어도 모듈에 알려 주지 않습니다. 그래서 새로 체크한 앱은
앱을 한 번 전환한 뒤에, 길게는 30초쯤 지나서 적용됩니다.

## 빌드

```sh
./gradlew :app:assembleDebug
```

릴리스 빌드는 프로젝트 최상위의 `keystore.properties`(`storeFile`,
`storePassword`, `keyAlias`, `keyPassword`)에 적은 키로 서명됩니다.
