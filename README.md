# ProfPay

## 🌐 Language
- [English](README.md)
- [Русский](README.ru.md)

## ⚠️ Disclaimer

**This project is currently in active development.**
While it is open for public testing, many features and components are still being worked on, and updates will be made regularly.
We encourage contributions and responsible bug reporting to help improve the project.

## 📜 License

This code is proprietary and not intended for public use.

It is shared **only for auditing and contribution purposes**.
Please see [LICENSE](./LICENSE) for full terms.

## 💼 About the Project

**ProfPay** is a cryptocurrency wallet designed for secure transactions using **USDT (TRC20)** and **TRX**.
The wallet is equipped with a smart contract system for handling USDT TRC20 transactions, providing advanced security features to protect users' funds.

Key features:
- **Main Address and Six Additional Slots**: The wallet has one primary address and six additional slots that act as protective barriers against dirty currency. If a slot receives dirty currency, it can be replaced with clean funds.
- **Multi-Currency Support**: Currently, the wallet supports **USDT** and **TRX**, with plans to add more cryptocurrencies in the future.

## ⚙️ Build and Installation (via Docker)

### 🧩 1. Generate the Keystore (JKS file)

To build a **release APK**, you must have a **Java Keystore (JKS)** file used for signing your Android app.
If you don’t already have one, generate it using:

```bash
keytool -genkeypair -v \
  -keystore profpay-release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias profpay
```

### 🧩 2. Export Required Environment Variables
Before building, export the following variables that will be used during the Docker build process:

```bash
export KEYSTORE_FILE=profpay-release-key.jks
export KEYSTORE_PASSWORD=password_from_keystore
export KEY_ALIAS=profpay
export KEY_PASSWORD=password_key
```

### 🧩 3. Build the Project via Docker
Run the following command from the root directory of your project:

```bash
sudo docker build -f docker/Dockerfile.release \
  --build-arg KEYSTORE_FILE=$KEYSTORE_FILE \
  --build-arg KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD \
  --build-arg KEY_ALIAS=$KEY_ALIAS \
  --build-arg KEY_PASSWORD=$KEY_PASSWORD \
  -t wallet-builder .
```

### 🧩 4. Extract the Final APK
After the build finishes, extract the generated .apk from the container:

```bash
docker run --rm -v $(pwd):/out wallet-builder cp /app/app-release.apk /out/
```

The signed `app-release.apk` will be copied to your current project directory:
```
./app-release.apk
```
