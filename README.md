# AI-Powered Family Location Tracker

**AI-Powered Family Location Tracker** is a completely free, zero-subscription app designed to keep your family connected. It offers real-time GPS tracking on an interactive map and securely logs a 30-day location history without heavily draining your device's battery. The standout feature is a built-in AI movement assistant powered by Google AI Studio. Instead of manually scrubbing through complex map timelines, you can simply type questions like, "Did the kids reach school today?" or "When did Mom leave the office?" and get instant, conversational updates based on the recorded data.

## Features
* **Live Location Tracking**: Real-time GPS tracking displayed on an interactive OpenStreetMap interface via Socket.io.
* **Conversational AI Assistant**: Ask natural language questions about location history using Google AI Studio (Gemini API) instead of scrubbing timelines.
* **Battery Optimized**: Background GPS logging utilizes native OS geofencing APIs to minimize battery drain (<10% per day).
* **Zero-Cost Architecture**: Built entirely on free-tier and open-source services with zero monthly subscriptions.
* **Auto-Purging Database**: MongoDB TTL indexes automatically delete records older than 30 days to stay within the 512MB free storage limit.

## Tech Stack
* **Frontend**: React Native or Flutter
* **Backend**: Node.js, Express.js
* **Real-time Engine**: Socket.io
* **Database**: MongoDB Atlas (M0 Free Cluster)
* **AI Engine**: Google Gen AI SDK (Google AI Studio)
* **Maps**: OpenStreetMap (OSM)

## Prerequisites
* Node.js (v18 or higher)
* React Native CLI or Flutter SDK
* MongoDB Atlas Account (Free M0 Cluster)
* Google AI Studio API Key

## Installation & Setup

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/family-location-tracker.git
cd family-location-tracker
```

2. **Backend Setup**
```bash
cd backend
npm install
```

3. **Frontend Setup**
```bash
cd ../mobile
npm install # for React Native
# or
flutter pub get # for Flutter
```

## Environment Variables
Create a `.env` file in your `backend` directory and add the following keys. Never commit this file to version control.

```env
PORT=3000
MONGODB_URI=your_mongodb_atlas_connection_string
GEMINI_API_KEY=your_google_ai_studio_key
JWT_SECRET=your_secure_random_string
```

## Usage
1. Start the Node.js backend server:
```bash
cd backend
npm start
```
2. Run the mobile application on your emulator or physical device:
```bash
cd mobile
npx react-native run-android # or run-ios
```
3. Create family accounts, log in, and background tracking will initialize automatically. Use the chat tab to query location history.

## License
Distributed under the MIT License. See `LICENSE` for more information.