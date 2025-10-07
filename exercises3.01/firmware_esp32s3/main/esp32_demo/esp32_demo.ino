#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>

// ====== CONFIG ======
const char* ssid = "CONHAMA";
const char* password = "22042004";

const char* mqtt_server = "broker.hivemq.com";
const int mqtt_port = 1883;

#define TOPIC_NS "demo/room1"
#define DEVICE_ID "esp32_sim_device"

// ====== GPIO ======
#define LED_PIN 2       // LED on-board (Light)
#define DHTPIN 4        // Chân DATA của DHT22 nối vào GPIO4
#define DHTTYPE DHT22   // Loại cảm biến

// ====== OBJECTS ======
WiFiClient espClient;
PubSubClient client(espClient);
DHT dht(DHTPIN, DHTTYPE);

// ====== DEVICE STATE ======
String light_state = "off";
String fan_state = "off";

// ====== WiFi setup ======
void setup_wifi() {
  Serial.println("🔌 Connecting to WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi connected!");
  Serial.print("📶 IP: ");
  Serial.println(WiFi.localIP());
}

// ====== MQTT reconnect ======
void reconnect() {
  while (!client.connected()) {
    Serial.print("🔄 Connecting to MQTT...");
    if (client.connect(DEVICE_ID)) {
      Serial.println("✅ Connected!");
      client.subscribe(TOPIC_NS "/device/cmd");

      // Publish online status
      String onlineMsg = "{\"online\":true}";
      client.publish(TOPIC_NS "/sys/online", onlineMsg.c_str(), true);
    } else {
      Serial.print("❌ failed, rc=");
      Serial.print(client.state());
      Serial.println(" retry in 5s");
      delay(5000);
    }
  }
}

// ====== Giả lập quạt (chỉ in ra Serial, on/off) ======
void runFan(String mode) {
  if (mode == "on") {
    fan_state = "on";
    Serial.println("🌀 Fan turned ON");
  } else {
    fan_state = "off";
    Serial.println("🌀 Fan turned OFF");
  }
}

// ====== Handle incoming MQTT commands ======
void callback(char* topic, byte* payload, unsigned int length) {
  String message;
  for (int i = 0; i < length; i++) message += (char)payload[i];

  Serial.print("📩 Received [");
  Serial.print(topic);
  Serial.print("]: ");
  Serial.println(message);

  if (String(topic) == TOPIC_NS "/device/cmd") {
    // Light control
    if (message.indexOf("light") >= 0) {
      if (message.indexOf("on") >= 0) {
        light_state = "on";
        digitalWrite(LED_PIN, HIGH);
      } else if (message.indexOf("off") >= 0) {
        light_state = "off";
        digitalWrite(LED_PIN, LOW);
      }
      Serial.println("💡 Light: " + light_state);
    }

    // Fan control (chỉ on/off)
    if (message.indexOf("fan") >= 0) {
      if (message.indexOf("on") >= 0) runFan("on");
      else if (message.indexOf("off") >= 0) runFan("off");
    }

    // Gửi lại trạng thái thật của thiết bị
    String stateMsg = "{\"light\":\"" + light_state + "\",\"fan\":\"" + fan_state + "\"}";
    client.publish(TOPIC_NS "/device/state", stateMsg.c_str(), true);
    Serial.println("📤 Published state: " + stateMsg);
  }
}

// ====== Publish sensor data ======
void publish_sensor_data() {
  float temp = dht.readTemperature();
  float hum = dht.readHumidity();
  int light = random(100, 800);

  // Nếu cảm biến không có thật, giả lập giá trị
  if (isnan(temp) || isnan(hum)) {
    temp = random(25, 35);
    hum = random(40, 70);
    Serial.println("⚠️ DHT22 not detected → using simulated data.");
  }

  String payload = "{\"ts\":" + String((unsigned long)time(NULL)) +
                   ",\"temp_c\":" + String(temp, 1) +
                   ",\"humidity\":" + String(hum, 1) +
                   ",\"light_lux\":" + String(light) + "}";
  client.publish(TOPIC_NS "/sensor/state", payload.c_str());
  Serial.println("🌡️ Sensor Data: " + payload);
}

// ====== Publish system info ======
void publish_system_info() {
  int rssi = WiFi.RSSI();
  String firmware = "v1.0.4-fanfix";
  unsigned long now = millis() / 1000;

  String sysPayload = "{\"wifi_signal\":" + String(rssi) +
                      ",\"firmware\":\"" + firmware +
                      "\",\"uptime\":" + String(now) + "}";
  client.publish(TOPIC_NS "/sys/info", sysPayload.c_str(), true);
  Serial.println("⚙️ System Info: " + sysPayload);
}

// ====== Setup ======
void setup() {
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  dht.begin();
  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

// ====== Loop ======
void loop() {
  if (!client.connected()) reconnect();
  client.loop();

  static unsigned long lastSensor = 0;
  static unsigned long lastSys = 0;

  if (millis() - lastSensor > 5000) {
    publish_sensor_data();
    lastSensor = millis();
  }

  if (millis() - lastSys > 10000) {
    publish_system_info();
    lastSys = millis();
  }
}
