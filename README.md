# Enterprise Fintech Multi-Dimensional AI Risk Guardian

An advanced AI-powered transaction fraud risk and decisioning engine built with:
- **Backend (Python)**: Multi-Factor Scoring Engine, Graph Mining, Website Legitimacy Evaluator, Dynamic Feedback, and Privacy-Preserving Federated Learning (`FedAvg`).
- **Frontend (Java)**: Modern high-contrast pure black GUI desktop client with real-time risk gauges, device simulation, and audit logging.

---

## 🏛️ Comprehensive Architecture & Features

### 1. Data Collection & Context Layer
- **Transaction Data**: Amount, timestamp, velocity, channel.
- **Device Fingerprint**: Device ID, IP origin country, VPN/TOR detection, Headless Emulator/Root detection.
- **Beneficiary Data**: KYC status, account age, dispute/chargeback count, transaction volume.
- **Merchant Website Inspection**: Domain age in days, SSL/TLS certificate validity & issuer, legal corporate business registration, and threat intelligence reputation score.

### 2. Customer–Device–Beneficiary Graph
- Tracks relationships in an in-memory bipartite graph.
- **Device-Sharing Ring Detection**: Flags devices associated with multiple distinct bank accounts (device farming / money mule syndicates).
- **Familiarity Verification**: Discounts risk for established historical edges.

### 3. Dual-Entity Risk Scoring
- **Customer Risk (0 – 100%)**: Evaluates spending deviation (vs 90-day moving average), balance liquidity drain ratio, velocity bursts ($1\text{h} / 24\text{h}$ frequency), and hardware binding.
- **Beneficiary Risk (0 – 100%)**: Evaluates KYC status, dispute count, fraud blacklist records, and merchant website legitimacy.

### 4. Bayesian Confidence & Uncertainty
- **AI Confidence Score ($50\% - 98.5\%$)**: Grows based on data completeness, account age, and KYC verification.
- **Bayesian Uncertainty ($1.5\% - 50\%$)**: Represents model entropy due to sparse data or novel accounts.

### 5. Action Decisioning Matrix (4 Strict Tiers)

| Composite Risk Score | Decision Action | System Behavior |
| :--- | :--- | :--- |
| **< 40%** | **🟢 APPROVE** | **Direct execution.** Low risk; automatically approved and processed immediately. |
| **40% – 60%** | **🟡 REVIEW / WARN** | **Cautionary prompt.** Displays reasons and requires sender confirmation before proceeding. |
| **60% – 80%** | **🟠 OTP / CHALLENGE** | **Suspended pending 2FA.** Requires 4-digit Security Authorization PIN (`1234`) to override. |
| **> 80%** | **🔴 BLOCK / STOP** | **Hard stop & rejection.** Permanently terminated; cannot be overridden to prevent loss. |

### 6. Explainable Threat Reason Codes
Every decision generates standardized audit reason codes:
- `RC_MERCHANT_DOMAIN_FRESH`: Merchant website registered < 15 days ago.
- `RC_MERCHANT_SSL_INVALID`: SSL/TLS certificate invalid or self-signed.
- `RC_MERCHANT_UNREGISTERED`: No corporate business registry or tax ID.
- `RC_DEV_GEO_MISMATCH`: IP country does not match account origin.
- `RC_DEV_VPN_TOR`: Transaction routed through commercial proxy or TOR.
- `RC_GRAPH_MULE_RING`: Device linked across multiple unrelated bank accounts.
- `RC_AMOUNT_OUTLIER_SEVERE`: Amount exceeds $8\times$ customer's 90-day average.
- `RC_HIGH_BALANCE_DRAIN`: Transfer consumes $> 70\%$ of available funds.

### 7. Dynamic Feedback Loop & Federated Learning
- **Trust Adaptation**: Legitimate transfers raise the customer's trust score ($+0.8\%$), lowering future risk baseline.
- **Federated Learning (`FedAvg`)**: Simulates privacy-preserving parameter updates across banking nodes with differential privacy—no raw customer PII is ever transmitted.

---

## 🚀 How to Run

1. Ensure the Python backend and Java frontend are launched:
   👉 Double-click [`start_all.bat`](file:///c:/Users/PROBOOK/Desktop/career/fintech-risk-engine/start_all.bat)

2. Or run manually:
   - **Backend**:
     ```powershell
     cd c:\Users\PROBOOK\Desktop\career\fintech-risk-engine\backend
     python server.py
     ```
   - **Frontend**:
     ```powershell
     cd c:\Users\PROBOOK\Desktop\career\fintech-risk-engine\frontend
     javac -d bin src\ApiClient.java src\FintechApp.java
     java -cp bin FintechApp
     ```
