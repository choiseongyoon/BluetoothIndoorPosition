# Bluetooth Indoor Position

블루투스 BLE Beacon를 통한 실내 위치 추정 어플리케이션

  - 개발 배경
  - 개발 Flow
  - 사용 기술
  - 프로그램 출력 결과


# 개발 배경
 - 4차 산업혁명으로 사물 인터넷 분야가 발전하고 있고 그중에서도 위치 기반 서비스가 급속도로 성장.
  - 흔히 알고 있는 GPS는 위성을 통해 위치 정보를 수신하기 때문에, 실내 환경에서는 사용하기 부적합.

따라서 :
  - 일상생활 속 많이 사용 중인 블루투스를 통해서 사용자 위치 추정 기술 연구를 개발. 


# 개발 Flow

1. MainActivity 
 - altbeacon api를 이용해서 주변 비콘들을 조회
 - Mac/Rssi/Txpower/Distance를 Listview로 표시
 - Listview Item 하나를 누르면 GraphActivity로 이동

2. GraphActivity
 - Distance의 변화를 Graph로 표현
 - 0.7초 간격으로 스캔된 비콘의 거리 계산
 - 총 100번의 측정 결과를 그래프로 표시
 
3. InActivity
 - 0.6초 간격으로 Rssi값이 큰 3개의 Beacon을 스캔
 - 삼변 측량을 통해 구해진 좌표값의 3번의 값을 평균값을 표시
 - 1.8초 간격으로 현재 위치 Update
 - Firebase에 각각의 user 폴더에  년-월-일 시:분:초로 세분화해서 저장



# 사용 기술 - FSPL(Free Space Path Loss)

![fspl](https://user-images.githubusercontent.com/29969821/42431041-e5c1967c-837d-11e8-9d9f-1af2077ead00.png)

FSPL를 통해 RSSI에 따른 거리를 추정

![12](https://user-images.githubusercontent.com/29969821/42431358-04235e46-8380-11e8-90c2-468c647e46e8.JPG)

FSPL로 계산된 거리와 임의의 3개의 Beacon의 AP (Access Point)를 삼변 측량 (Trilateration) 공식에 대입해 현재 위치를 추정

# 프로그램 출력 결과

![screenshot_2018-05-08-12-39-22](https://user-images.githubusercontent.com/29969821/42431215-272350a0-837f-11e8-87a5-28122e914862.png)
 - MainAcitivity에서 주변 Beacon 조회
 - Listview Item 하나를 누르면 GraphActivity로 이동
 - 상단 Position 버튼 누르면 InActivity로 이동
![screenshot_2018-05-08-12-43-36](https://user-images.githubusercontent.com/29969821/42431232-3b4405c0-837f-11e8-9f36-39e56d1c4431.png)

 - GraphAcitivity에서 선택한 Beacon의 추정된 Distance를 Graph로 표시

![screenshot_2018-05-15-14-58-30](https://user-images.githubusercontent.com/29969821/42431258-5c9c6154-837f-11e8-892e-d9a922ca4a08.png)
스캔된 비콘들 중 제일 가까운 3개를 삼변측량 공식에 대입해 현재 위치 표시
