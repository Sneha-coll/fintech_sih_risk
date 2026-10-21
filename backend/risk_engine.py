import json
import os
import math
import random
from datetime import datetime, timedelta

class RelationshipGraph:
    """
    In-memory graph modeling relationships between Customers, Devices, and Beneficiaries.
    Detects device-sharing rings, money mule syndicates, and network centrality.
    """
    def __init__(self):
        self.customer_devices = {}     # customer_id -> set of device_ids
        self.device_customers = {}     # device_id -> set of customer_ids
        self.customer_beneficiaries = {} # customer_id -> set of beneficiary_ids
        self.device_beneficiaries = {} # device_id -> set of beneficiary_ids

    def load_from_db(self, db):
        self.customer_devices.clear()
        self.device_customers.clear()
        self.customer_beneficiaries.clear()
        self.device_beneficiaries.clear()

        # Seed relationships from historical accounts and transactions
        for tx in db.get("history", []):
            c = tx.get("sender")
            b = tx.get("recipient")
            d = tx.get("device_id", "DEV-DEFAULT")
            if c and d:
                self.add_customer_device(c, d)
            if c and b:
                self.customer_beneficiaries.setdefault(c, set()).add(b)
            if d and b:
                self.device_beneficiaries.setdefault(d, set()).add(b)

    def add_customer_device(self, customer_id, device_id):
        self.customer_devices.setdefault(customer_id, set()).add(device_id)
        self.device_customers.setdefault(device_id, set()).add(customer_id)

    def add_interaction(self, customer_id, device_id, beneficiary_id):
        self.add_customer_device(customer_id, device_id)
        self.customer_beneficiaries.setdefault(customer_id, set()).add(beneficiary_id)
        self.device_beneficiaries.setdefault(device_id, set()).add(beneficiary_id)

    def analyze_graph(self, customer_id, device_id, beneficiary_id):
        """
        Analyzes graph structure for suspicious link patterns.
        """
        risk_contribution = 0.0
        reasons = []
        reason_codes = []

        # 1. Device Sharing / Mule Farm Check
        customers_on_device = self.device_customers.get(device_id, set())
        other_accounts = customers_on_device - {customer_id}
        sharing_degree = len(other_accounts)

        if sharing_degree >= 3:
            risk_contribution += 35.0
            reasons.append(f"Syndicate Alert: Device '{device_id}' is shared across {sharing_degree + 1} different bank accounts (Device Farm indicator)")
            reason_codes.append("RC_GRAPH_MULE_RING")
        elif sharing_degree >= 1:
            risk_contribution += 14.0
            reasons.append(f"Shared Hardware: Device '{device_id}' is linked to {sharing_degree} other customer account(s)")
            reason_codes.append("RC_GRAPH_SHARED_DEVICE")

        # 2. Known Relationship check
        known_beneficiaries = self.customer_beneficiaries.get(customer_id, set())
        is_known = beneficiary_id in known_beneficiaries
        if is_known:
            risk_contribution -= 10.0
            reasons.append("Graph Link Verified: Customer has established direct transfer history with this beneficiary")
            reason_codes.append("RC_GRAPH_KNOWN_BENEFICIARY")
        else:
            risk_contribution += 8.0
            reasons.append("First-degree graph novelty: No prior transactional edge exists between customer and beneficiary")
            reason_codes.append("RC_GRAPH_NEW_EDGE")

        return {
            "graph_risk": risk_contribution,
            "shared_device_count": len(customers_on_device),
            "is_known_edge": is_known,
            "reasons": reasons,
            "reason_codes": reason_codes
        }


class FederatedLearningSimulator:
    """
    Simulates Privacy-Preserving Federated Learning (FedAvg) across banking nodes.
    Updates model parameters (weights for amount, velocity, device risk, website risk)
    without transferring raw customer PII or transaction records.
    """
    def __init__(self):
        self.round_number = 14
        self.global_loss = 0.0842
        self.participating_nodes = 8
        self.weights = {
            "w_amount_deviation": 0.28,
            "w_velocity": 0.22,
            "w_device_risk": 0.20,
            "w_merchant_legitimacy": 0.18,
            "w_graph_centrality": 0.12
        }

    def train_federated_round(self, local_loss=0.079):
        self.round_number += 1
        # Simulated Federated Averaging parameter aggregation with differential privacy noise
        delta = random.uniform(-0.005, 0.005)
        self.global_loss = max(0.041, round(self.global_loss * 0.96 + delta, 4))
        return {
            "round": self.round_number,
            "global_loss": self.global_loss,
            "participating_nodes": self.participating_nodes,
            "model_version": f"FedAvg-v2.{self.round_number}",
            "privacy_mechanism": "Differential Privacy (Epsilon=0.5, Delta=1e-5)"
        }


class AdvancedRiskEngine:
    def __init__(self, data_file="database.json"):
        self.data_file = data_file
        self.graph = RelationshipGraph()
        self.federated = FederatedLearningSimulator()
        self.load_data()

    def load_data(self):
        try:
            with open(self.data_file, "r") as f:
                self.db = json.load(f)
                # Ensure latest architecture seed entities
                default_data = self._get_default_database()
                for acc_id, acc_data in default_data["accounts"].items():
                    if acc_id not in self.db.get("accounts", {}):
                        self.db.setdefault("accounts", {})[acc_id] = acc_data
                self.save_data()
        except Exception:
            self.db = self._get_default_database()
            self.save_data()

        self.graph.load_from_db(self.db)

    def save_data(self):
        with open(self.data_file, "w") as f:
            json.dump(self.db, f, indent=2)

    def _get_default_database(self):
        return {
            "accounts": {
                "ACC-1001": {
                    "account_number": "ACC-1001",
                    "owner_name": "Alice Johnson (Primary Sender)",
                    "balance": 15450.00,
                    "created_date": "2023-01-15",
                    "kyc_verified": True,
                    "avg_transaction_amount": 250.00,
                    "tx_velocity_1h": 0,
                    "tx_velocity_24h": 2,
                    "home_country": "US",
                    "registered_devices": ["DEV-TRUSTED-IPHONE15", "DEV-WORK-MACBOOK"],
                    "trust_score": 92.0,
                    "dispute_count": 0,
                    "fraud_flag": False
                },
                "ACC-1002": {
                    "account_number": "ACC-1002",
                    "owner_name": "Bob Smith (Secondary Sender)",
                    "balance": 48200.00,
                    "created_date": "2022-08-10",
                    "kyc_verified": True,
                    "avg_transaction_amount": 500.00,
                    "tx_velocity_1h": 1,
                    "tx_velocity_24h": 4,
                    "home_country": "US",
                    "registered_devices": ["DEV-BOB-LAPTOP"],
                    "trust_score": 88.0,
                    "dispute_count": 0,
                    "fraud_flag": False
                },
                # Tier 1: Low Risk (< 40%) - Instant Approval
                "ACC-9001": {
                    "account_number": "ACC-9001",
                    "owner_name": "Amazon Retail Hub [Low Risk <40%]",
                    "balance": 120000.00,
                    "created_date": "2021-04-12",
                    "kyc_verified": True,
                    "total_transactions_count": 1420,
                    "dispute_count": 0,
                    "fraud_flag": False,
                    "website": {
                        "url": "https://pay.amazon-hub.com",
                        "domain_age_days": 1820,
                        "ssl_valid": True,
                        "ssl_issuer": "DigiCert Global EV Root CA",
                        "business_registered": True,
                        "tax_id_verified": True,
                        "reputation_score": 98.0
                    }
                },
                "ACC-9002": {
                    "account_number": "ACC-9002",
                    "owner_name": "Robert Taylor (Verified Contact) [Low Risk <40%]",
                    "balance": 4200.00,
                    "created_date": "2023-06-20",
                    "kyc_verified": True,
                    "total_transactions_count": 35,
                    "dispute_count": 0,
                    "fraud_flag": False,
                    "website": None
                },
                # Tier 2: 40% - 60% (Moderate Risk / Warning)
                "ACC-9004": {
                    "account_number": "ACC-9004",
                    "owner_name": "TechDeals Global (Unverified Vendor) [Warning 40%-60%]",
                    "balance": 3100.00,
                    "created_date": "2026-10-02",
                    "kyc_verified": False,
                    "total_transactions_count": 4,
                    "dispute_count": 1,
                    "fraud_flag": False,
                    "website": {
                        "url": "http://techdeals-quickpay.xyz",
                        "domain_age_days": 19,
                        "ssl_valid": False,
                        "ssl_issuer": "Self-Signed Untrusted",
                        "business_registered": False,
                        "tax_id_verified": False,
                        "reputation_score": 42.0
                    }
                },
                "ACC-9005": {
                    "account_number": "ACC-9005",
                    "owner_name": "Elena Rostova (Overseas Freelance) [Warning 40%-60%]",
                    "balance": 1850.00,
                    "created_date": "2026-09-18",
                    "kyc_verified": False,
                    "total_transactions_count": 7,
                    "dispute_count": 1,
                    "fraud_flag": False,
                    "website": None
                },
                # Tier 3: 60% - 80% (High Risk / Blocked - Requires PIN Permission)
                "ACC-9006": {
                    "account_number": "ACC-9006",
                    "owner_name": "FastLoan Liquidity Pool [Blocked 60%-80%]",
                    "balance": 620.00,
                    "created_date": "2026-10-12",
                    "kyc_verified": False,
                    "total_transactions_count": 8,
                    "dispute_count": 4,
                    "fraud_flag": False,
                    "website": {
                        "url": "https://fastloan-pool-claim.live",
                        "domain_age_days": 6,
                        "ssl_valid": False,
                        "ssl_issuer": "Untrusted / Invalid Host",
                        "business_registered": False,
                        "tax_id_verified": False,
                        "reputation_score": 15.0
                    }
                },
                "ACC-9007": {
                    "account_number": "ACC-9007",
                    "owner_name": "Apex Offshore Escrow [Blocked 60%-80%]",
                    "balance": 980.00,
                    "created_date": "2026-10-14",
                    "kyc_verified": False,
                    "total_transactions_count": 5,
                    "dispute_count": 3,
                    "fraud_flag": False,
                    "website": None
                },
                # Tier 4: > 80% (Critical Risk / Stopped & Rejected)
                "ACC-9003": {
                    "account_number": "ACC-9003",
                    "owner_name": "Crypto Swift Mixer (Blacklisted) [Stopped >80%]",
                    "balance": 250.00,
                    "created_date": "2026-10-19",
                    "kyc_verified": False,
                    "total_transactions_count": 2,
                    "dispute_count": 6,
                    "fraud_flag": True,
                    "website": {
                        "url": "https://swift-mixer-tor-gateway.cc",
                        "domain_age_days": 3,
                        "ssl_valid": False,
                        "ssl_issuer": "Revoked / Expired",
                        "business_registered": False,
                        "tax_id_verified": False,
                        "reputation_score": 5.0
                    }
                }
            },
            "history": [
                {
                    "tx_id": "TXN-8801",
                    "sender": "ACC-1001",
                    "recipient": "ACC-9001",
                    "amount": 120.00,
                    "risk_score": 12.0,
                    "customer_risk": 8.0,
                    "beneficiary_risk": 5.0,
                    "confidence": 94.0,
                    "uncertainty": 6.0,
                    "device_id": "DEV-TRUSTED-IPHONE15",
                    "decision": "APPROVE",
                    "status": "COMPLETED",
                    "timestamp": "2026-10-19 14:22:10"
                },
                {
                    "tx_id": "TXN-8802",
                    "sender": "ACC-1001",
                    "recipient": "ACC-9004",
                    "amount": 350.00,
                    "risk_score": 48.0,
                    "customer_risk": 22.0,
                    "beneficiary_risk": 58.0,
                    "confidence": 88.0,
                    "uncertainty": 12.0,
                    "device_id": "DEV-TRUSTED-IPHONE15",
                    "decision": "REVIEW",
                    "status": "COMPLETED_WITH_WARNING",
                    "timestamp": "2026-10-20 09:15:30"
                }
            ]
        }

    def evaluate_website_legitimacy(self, website_data):
        """
        Evaluates merchant domain age, SSL security, legal registration, and reputation.
        """
        if not website_data:
            return {"risk": 0.0, "reasons": [], "codes": []}

        risk = 0.0
        reasons = []
        codes = []

        domain_age = website_data.get("domain_age_days", 365)
        if domain_age < 15:
            risk += 28.0
            reasons.append(f"Domain Freshness Threat: Merchant website is only {domain_age} days old (Common scam disposable domain)")
            codes.append("RC_MERCHANT_DOMAIN_FRESH")
        elif domain_age < 60:
            risk += 14.0
            reasons.append(f"Recent Domain: Website registered less than 60 days ago ({domain_age} days)")
            codes.append("RC_MERCHANT_DOMAIN_RECENT")

        if not website_data.get("ssl_valid", True):
            risk += 25.0
            reasons.append("Insecure Web Channel: SSL/TLS certificate is invalid, untrusted, or self-signed")
            codes.append("RC_MERCHANT_SSL_INVALID")

        if not website_data.get("business_registered", True):
            risk += 20.0
            reasons.append("Unregistered Business Entity: No legal corporate business registry or tax filing on record")
            codes.append("RC_MERCHANT_UNREGISTERED")

        rep = website_data.get("reputation_score", 100.0)
        if rep < 40.0:
            risk += 18.0
            reasons.append(f"Poor Merchant Web Reputation: Trust rating is only {rep:.0f}/100 based on threat intelligence feeds")
            codes.append("RC_MERCHANT_POOR_REPUTATION")

        return {"risk": min(65.0, risk), "reasons": reasons, "codes": codes}

    def evaluate_device_risk(self, sender, device_info):
        """
        Evaluates device fingerprint, IP geolocation, VPN/TOR proxy, and device binding.
        """
        risk = 0.0
        reasons = []
        codes = []

        dev_id = device_info.get("device_id", "DEV-TRUSTED-IPHONE15")
        ip_country = device_info.get("ip_country", "US")
        is_vpn = device_info.get("is_vpn", False)
        is_emulator = device_info.get("is_emulator", False)

        # 1. Registered Device Check
        reg_devices = sender.get("registered_devices", [])
        if dev_id not in reg_devices:
            risk += 20.0
            reasons.append(f"Unrecognized Hardware: Device '{dev_id}' is not in customer's trusted device registry")
            codes.append("RC_DEV_NEW_DEVICE")

        # 2. Geolocation Mismatch
        home_country = sender.get("home_country", "US")
        if ip_country != home_country:
            risk += 25.0
            reasons.append(f"Geographic Distance Anomaly: Transaction IP origin ({ip_country}) does not match account origin ({home_country})")
            codes.append("RC_DEV_GEO_MISMATCH")

        # 3. Anonymizer / VPN / Tor
        if is_vpn:
            risk += 22.0
            reasons.append("Anonymizing Proxy: Transaction routed through commercial VPN / TOR exit node")
            codes.append("RC_DEV_VPN_TOR")

        # 4. Device Integrity (Root / Emulator)
        if is_emulator:
            risk += 35.0
            reasons.append("Compromised Environment: Device identified as headless emulator or rooted runtime")
            codes.append("RC_DEV_EMULATOR_ROOTED")

        return {"risk": min(60.0, risk), "device_id": dev_id, "reasons": reasons, "codes": codes}

    def evaluate_risk(self, sender_id, recipient_id, amount, device_info=None):
        """
        Comprehensive Multi-Dimensional Fintech AI Risk Evaluation.
        Separates Customer Risk and Beneficiary Risk, builds Graph linkage,
        inspects Merchant Website legitimacy, and estimates Confidence and Uncertainty.
        """
        sender = self.db["accounts"].get(sender_id)
        recipient = self.db["accounts"].get(recipient_id)

        if not sender:
            return {"error": f"Sender account {sender_id} not found", "risk_score": 100.0, "decision": "BLOCK"}
        if not recipient:
            return {"error": f"Recipient account {recipient_id} not found", "risk_score": 100.0, "decision": "BLOCK"}
        if amount <= 0:
            return {"error": "Amount must be strictly positive", "risk_score": 100.0, "decision": "BLOCK"}

        if device_info is None:
            device_info = {
                "device_id": sender.get("registered_devices", ["DEV-TRUSTED-IPHONE15"])[0],
                "ip_country": sender.get("home_country", "US"),
                "is_vpn": False,
                "is_emulator": False
            }

        all_reasons = []
        all_codes = []

        # ==============================================================
        # 1. CUSTOMER RISK CALCULATION (0 - 100)
        # ==============================================================
        c_risk = 5.0

        # Solvency & Liquidity Drain
        if amount > sender["balance"]:
            c_risk += 90.0
            all_reasons.append(f"Overdraft Hazard: Transfer ${amount:,.2f} exceeds available balance ${sender['balance']:,.2f}")
            all_codes.append("RC_INSUFFICIENT_FUNDS")
        else:
            drain = amount / (sender["balance"] + 1.0)
            if drain > 0.70:
                c_risk += 25.0
                all_reasons.append(f"High Capital Drain: Transaction exhausts {drain*100:.1f}% of entire sender account balance")
                all_codes.append("RC_HIGH_BALANCE_DRAIN")
            elif drain > 0.40:
                c_risk += 12.0
                all_reasons.append(f"Noticeable balance consumption ({drain*100:.1f}% of balance)")
                all_codes.append("RC_MODERATE_BALANCE_DRAIN")

        # Spending Deviation vs 90d Average
        avg_amt = max(1.0, sender.get("avg_transaction_amount", 250.0))
        ratio = amount / avg_amt
        if ratio > 8.0:
            c_risk += 28.0
            all_reasons.append(f"Severe Spending Spike: {ratio:.1f}x higher than your historical average (${avg_amt:,.2f})")
            all_codes.append("RC_AMOUNT_OUTLIER_SEVERE")
        elif ratio > 3.0:
            c_risk += 16.0
            all_reasons.append(f"Spending Deviation: {ratio:.1f}x higher than normal average (${avg_amt:,.2f})")
            all_codes.append("RC_AMOUNT_OUTLIER_MODERATE")

        # Velocity Spikes (Frequency in 1h / 24h)
        v_1h = sender.get("tx_velocity_1h", 0)
        v_24h = sender.get("tx_velocity_24h", 0)
        if v_1h >= 3:
            c_risk += 20.0
            all_reasons.append(f"Velocity Burst: {v_1h} transactions attempted within the past 60 minutes")
            all_codes.append("RC_VELOCITY_BURST_1H")
        elif v_24h >= 8:
            c_risk += 12.0
            all_reasons.append(f"High Daily Velocity: {v_24h} transfers in the last 24-hour window")
            all_codes.append("RC_VELOCITY_HIGH_24H")

        # Device Risk evaluation for Customer
        dev_eval = self.evaluate_device_risk(sender, device_info)
        c_risk += dev_eval["risk"]
        all_reasons.extend(dev_eval["reasons"])
        all_codes.extend(dev_eval["codes"])

        # Customer Trust Score dampening
        trust_discount = (sender.get("trust_score", 90.0) - 50.0) * 0.15
        c_risk = max(0.0, min(100.0, c_risk - trust_discount))

        # ==============================================================
        # 2. BENEFICIARY & MERCHANT WEBSITE RISK CALCULATION (0 - 100)
        # ==============================================================
        b_risk = 5.0

        if recipient.get("fraud_flag", False):
            b_risk += 75.0
            all_reasons.append("CRITICAL: Beneficiary is actively blacklisted / flagged in global anti-fraud repository")
            all_codes.append("RC_BENEFICIARY_BLACKLISTED")

        disputes = recipient.get("dispute_count", 0)
        if disputes >= 4:
            b_risk += 28.0
            all_reasons.append(f"Reputation Alert: Beneficiary has {disputes} recorded chargeback / fraud complaints")
            all_codes.append("RC_BENEFICIARY_EXCESSIVE_DISPUTES")
        elif disputes >= 2:
            b_risk += 16.0
            all_reasons.append(f"Beneficiary has {disputes} chargeback dispute(s) on record")
            all_codes.append("RC_BENEFICIARY_DISPUTES")
        elif disputes == 1:
            b_risk += 8.0
            all_reasons.append("Beneficiary has 1 past customer dispute on record")
            all_codes.append("RC_BENEFICIARY_MINOR_DISPUTE")

        if not recipient.get("kyc_verified", True):
            b_risk += 14.0
            all_reasons.append("Unverified Entity: Beneficiary has NOT completed KYC identity verification")
            all_codes.append("RC_BENEFICIARY_UNVERIFIED_KYC")

        rec_tx_cnt = recipient.get("total_transactions_count", 0)
        if rec_tx_cnt <= 3:
            b_risk += 12.0
            all_reasons.append(f"Account Freshness Risk: Beneficiary has near-zero network history ({rec_tx_cnt} lifetime txns)")
            all_codes.append("RC_BENEFICIARY_NEW_ACCOUNT")
        elif rec_tx_cnt <= 8:
            b_risk += 6.0
            all_reasons.append(f"Limited network track record ({rec_tx_cnt} lifetime txns)")
            all_codes.append("RC_BENEFICIARY_LIMITED_HISTORY")

        # Merchant Website Legitimacy Analysis
        web_eval = self.evaluate_website_legitimacy(recipient.get("website"))
        b_risk += web_eval["risk"]
        all_reasons.extend(web_eval["reasons"])
        all_codes.extend(web_eval["codes"])

        b_risk = max(0.0, min(100.0, b_risk))

        # ==============================================================
        # 3. RELATIONSHIP GRAPH LINK ANALYSIS
        # ==============================================================
        graph_eval = self.graph.analyze_graph(sender_id, device_info.get("device_id"), recipient_id)
        graph_risk = graph_eval["graph_risk"]
        all_reasons.extend(graph_eval["reasons"])
        all_codes.extend(graph_eval["reason_codes"])

        # ==============================================================
        # 4. COMPOSITE ENSEMBLE RISK SCORE ESTIMATION
        # ==============================================================
        # If recipient is blacklisted for fraud, hard stop (>80%)
        if recipient.get("fraud_flag", False):
            final_risk = max(92.0, min(100.0, b_risk))
        else:
            # Weighted ensemble combining Customer Risk (35%), Beneficiary/Website Risk (50%), and Graph/Device Risk (15%)
            composite = (0.35 * c_risk) + (0.50 * b_risk) + (0.15 * (dev_eval["risk"] + graph_risk))
            final_risk = max(0.0, min(100.0, round(composite, 1)))

        # ==============================================================
        # 5. CONFIDENCE AND UNCERTAINTY ESTIMATION
        # ==============================================================
        # Based on data availability, historical sample sizes, KYC presence
        sample_size = sender.get("total_transactions_count", 50) + recipient.get("total_transactions_count", 5)
        has_kyc = sender.get("kyc_verified", True) and recipient.get("kyc_verified", False)
        
        # Bayesian evidence growth curve
        base_confidence = 65.0 + min(30.0, (sample_size / 40.0) * 15.0) + (8.0 if has_kyc else 0.0)
        confidence = round(min(98.5, max(50.0, base_confidence)), 1)
        uncertainty = round(100.0 - confidence, 1)

        # ==============================================================
        # 6. ACTION SELECTION (MAPPING TO 4 TIERS)
        # ==============================================================
        # > 80%: BLOCK / STOP (Rejected)
        # 60% - 80%: OTP / CHALLENGE (Blocked - asks for PIN 1234)
        # 40% - 60%: REVIEW / WARN (Warning prompt before proceeding)
        # < 40%: APPROVE (Direct frictionless execution)
        if final_risk > 80.0:
            action = "BLOCK"
            decision_label = "BLOCK / STOP (>80%)"
            policy_action = "Transaction aborted by Security Protocol. Critical risk exceeds 80% tolerance threshold."
        elif 60.0 <= final_risk <= 80.0:
            action = "OTP"
            decision_label = "OTP / CHALLENGE (60%-80%)"
            policy_action = "High risk detected (60%-80%). Suspended pending mandatory 2FA / Security PIN authorization."
        elif 40.0 <= final_risk < 60.0:
            action = "REVIEW"
            decision_label = "REVIEW / WARN (40%-60%)"
            policy_action = "Moderate risk warning (40%-60%). Requires explicit user acknowledgement to proceed."
        else:
            action = "APPROVE"
            decision_label = "APPROVE (<40%)"
            policy_action = "Low risk (<40%). Approved for instant, frictionless automated execution."

        # Federated Learning status metadata
        fed_info = {
            "model_version": f"FedAvg-v2.{self.federated.round_number}",
            "federated_round": self.federated.round_number,
            "global_loss": self.federated.global_loss,
            "privacy": "Zero raw PII exchanged (Federated Differentially-Private Aggregation)"
        }

        return {
            "sender_id": sender_id,
            "sender_name": sender["owner_name"],
            "recipient_id": recipient_id,
            "recipient_name": recipient["owner_name"],
            "amount": amount,
            "risk_score": final_risk,
            "customer_risk": round(c_risk, 1),
            "beneficiary_risk": round(b_risk, 1),
            "confidence": confidence,
            "uncertainty": uncertainty,
            "action": action,
            "decision": decision_label,
            "policy_action": policy_action,
            "reasons": all_reasons,
            "reason_codes": list(dict.fromkeys(all_codes)), # Deduplicate
            "device_data": {
                "device_id": device_info.get("device_id"),
                "is_registered": device_info.get("device_id") in sender.get("registered_devices", []),
                "ip_country": device_info.get("ip_country"),
                "shared_device_count": graph_eval["shared_device_count"]
            },
            "website_data": recipient.get("website"),
            "federated_learning": fed_info
        }

    def execute_transaction(self, sender_id, recipient_id, amount, override_authorized=False, device_info=None):
        assessment = self.evaluate_risk(sender_id, recipient_id, amount, device_info)
        if "error" in assessment and "Overdraft Hazard" in str(assessment.get("reasons")):
            return {"success": False, "assessment": assessment, "message": "Transaction failed: Insufficient balance."}

        action = assessment["action"]

        # > 80%: BLOCK - cannot be overridden
        if action == "BLOCK":
            return {
                "success": False,
                "assessment": assessment,
                "message": "TRANSACTION BLOCKED & STOPPED: Risk exceeds 80%. Aborted to prevent fraud."
            }

        # 60% - 80%: OTP Challenge requires PIN override
        if action == "OTP" and not override_authorized:
            return {
                "success": False,
                "assessment": assessment,
                "message": "TRANSACTION CHALLENGED: 60%-80% risk requires Security PIN / OTP permission."
            }

        sender = self.db["accounts"][sender_id]
        recipient = self.db["accounts"][recipient_id]

        if sender["balance"] < amount:
            return {"success": False, "assessment": assessment, "message": "Transaction failed: Insufficient balance."}

        # Process balances
        sender["balance"] -= amount
        recipient["balance"] += amount
        sender["total_transactions_count"] = sender.get("total_transactions_count", 0) + 1
        recipient["total_transactions_count"] = recipient.get("total_transactions_count", 0) + 1
        sender["tx_velocity_1h"] = sender.get("tx_velocity_1h", 0) + 1
        sender["tx_velocity_24h"] = sender.get("tx_velocity_24h", 0) + 1

        # Dynamic Trust Adjustment (Legitimate activity lowers risk by increasing trust score)
        sender["trust_score"] = min(99.0, round(sender.get("trust_score", 90.0) + 0.8, 1))

        # Dynamic Moving Average update
        sender["avg_transaction_amount"] = round((sender.get("avg_transaction_amount", amount) * 0.9) + (amount * 0.1), 2)

        dev_id = assessment["device_data"]["device_id"]

        # Update In-Memory Graph with new confirmed edge
        self.graph.add_interaction(sender_id, dev_id, recipient_id)

        # Trigger simulated federated learning parameter update
        fed_update = self.federated.train_federated_round()

        tx_record = {
            "tx_id": f"TXN-{int(datetime.now().timestamp() * 1000) % 1000000}",
            "sender": sender_id,
            "recipient": recipient_id,
            "amount": amount,
            "risk_score": assessment["risk_score"],
            "customer_risk": assessment["customer_risk"],
            "beneficiary_risk": assessment["beneficiary_risk"],
            "confidence": assessment["confidence"],
            "uncertainty": assessment["uncertainty"],
            "action": action,
            "decision": assessment["decision"],
            "status": "APPROVED_OTP_OVERRIDE" if override_authorized and action != "APPROVE" else ("APPROVED_WITH_WARNING" if action == "REVIEW" else "APPROVED"),
            "device_id": dev_id,
            "reason_codes": assessment["reason_codes"],
            "federated_model_version": fed_update["model_version"],
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }

        self.db["history"].insert(0, tx_record)
        self.save_data()

        return {
            "success": True,
            "assessment": assessment,
            "tx_record": tx_record,
            "message": "Transaction approved and audit log recorded."
        }
