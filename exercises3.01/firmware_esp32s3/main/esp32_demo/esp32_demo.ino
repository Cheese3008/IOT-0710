#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <ArduinoJson.h>  // ✅ thêm thư viện để parse JSON

// ====== CONFIG ======
const char* ssid = "Duc Tri";
const char* password = "22091977";

const char* mqtt_server = "broker.hivemq.com";
const int mqtt_port = 1883;

#define TOPIC_NS "demo/room1"
#define DEVICE_ID "esp32_real_device"

// ====== GPIO ======
#define LED_PIN 5
#define DHTPIN 4
#define DHTTYPE DHT22

// --- L298N ---
#define IN1_PIN 26
#define IN2_PIN 27
#define ENA_PIN 25

// ====== OBJECTS ======
WiFiClient espClient;
PubSubClient client(espClient);
DHT dht(DHTPIN, DHTTYPE);

// ====== STATE ======
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

      // Gửi trạng thái online
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

// ====== Fan Control ======
void controlFan(String cmd) {
  if (cmd == "on") {
    digitalWrite(IN1_PIN, HIGH);
    digitalWrite(IN2_PIN, LOW);
    digitalWrite(ENA_PIN, HIGH);
    fan_state = "on";
    Serial.println("🌀 Fan: ON");
  } else {
    digitalWrite(IN1_PIN, LOW);
    digitalWrite(IN2_PIN, LOW);
    digitalWrite(ENA_PIN, LOW);
    fan_state = "off";
    Serial.println("🌀 Fan: OFF");
  }
}

// ====== Handle incoming MQTT ======
void callback(char* topic, byte* payload, unsigned int length) {
  String message;
  for (int i = 0; i < length; i++) message += (char)payload[i];
  Serial.print("📩 Received [");
  Serial.print(topic);
  Serial.print("]: ");
  Serial.println(message);

  if (String(topic) == TOPIC_NS "/device/cmd") {
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, message);

    if (error) {
      Serial.println("⚠️ JSON parse error!");
      return;
    }

    // --- Light control ---
    if (doc.containsKey("light")) {
      String val = doc["light"].as<String>();
      if (val == "on") {
        digitalWrite(LED_PIN, HIGH);
        light_state = "on";
      } else {
        digitalWrite(LED_PIN, LOW);
        light_state = "off";
      }
      Serial.println("💡 Light: " + light_state);
    }

    // --- Fan control ---
    if (doc.containsKey("fan")) {
      String val = doc["fan"].as<String>();
      controlFan(val);
    }

    // --- Gửi lại trạng thái thật ---
    String stateMsg = "{\"light\":\"" + light_state + "\",\"fan\":\"" + fan_state + "\"}";
    client.publish(TOPIC_NS "/device/state", stateMsg.c_str(), true);
  }
}

// ====== Publish sensor data ======
void publish_sensor_data() {
  float temp = dht.readTemperature();
  float hum = dht.readHumidity();
  int light = random(100, 800);

  if (isnan(temp) || isnan(hum)) {
    Serial.println("⚠️ Lỗi đọc cảm biến DHT22!");
    return;
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
  String firmware = "v1.0.3";
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
  pinMode(IN1_PIN, OUTPUT);
  pinMode(IN2_PIN, OUTPUT);
  pinMode(ENA_PIN, OUTPUT);

  digitalWrite(LED_PIN, LOW);
  controlFan("off");

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
