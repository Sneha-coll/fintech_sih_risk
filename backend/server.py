import json
import os
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse
from risk_engine import AdvancedRiskEngine

PORT = 5000
engine = AdvancedRiskEngine(os.path.join(os.path.dirname(__file__), "database.json"))

class FintechRequestHandler(BaseHTTPRequestHandler):
    def _set_headers(self, status=200, content_type="application/json"):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.end_headers()

    def do_OPTIONS(self):
        self._set_headers(200)

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path

        if path == "/api/accounts":
            accounts_list = list(engine.db["accounts"].values())
            self._set_headers(200)
            self.wfile.write(json.dumps({"status": "success", "accounts": accounts_list}).encode("utf-8"))

        elif path == "/api/history":
            history = engine.db.get("history", [])
            self._set_headers(200)
            self.wfile.write(json.dumps({"status": "success", "history": history}).encode("utf-8"))

        elif path == "/api/status":
            self._set_headers(200)
            self.wfile.write(json.dumps({
                "status": "online",
                "service": "Fintech Multi-Dimensional AI Risk Guardian",
                "version": "2.0.0",
                "federated_model": f"FedAvg-v2.{engine.federated.round_number}"
            }).encode("utf-8"))

        elif path == "/api/federated-status":
            self._set_headers(200)
            self.wfile.write(json.dumps({
                "status": "success",
                "federated_learning": {
                    "round": engine.federated.round_number,
                    "global_loss": engine.federated.global_loss,
                    "participating_nodes": engine.federated.participating_nodes,
                    "weights": engine.federated.weights
                }
            }).encode("utf-8"))

        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Endpoint not found"}).encode("utf-8"))

    def do_POST(self):
        parsed = urlparse(self.path)
        path = parsed.path

        content_length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(content_length).decode("utf-8") if content_length > 0 else "{}"
        
        try:
            data = json.loads(body)
        except Exception:
            data = {}

        if path == "/api/evaluate-risk":
            sender_id = data.get("sender_id")
            recipient_id = data.get("recipient_id")
            try:
                amount = float(data.get("amount", 0))
            except (ValueError, TypeError):
                amount = 0.0

            device_info = data.get("device_info")
            result = engine.evaluate_risk(sender_id, recipient_id, amount, device_info)
            self._set_headers(200)
            self.wfile.write(json.dumps(result).encode("utf-8"))

        elif path == "/api/execute":
            sender_id = data.get("sender_id")
            recipient_id = data.get("recipient_id")
            try:
                amount = float(data.get("amount", 0))
            except (ValueError, TypeError):
                amount = 0.0
            override_authorized = bool(data.get("override_authorized", False))
            device_info = data.get("device_info")

            result = engine.execute_transaction(sender_id, recipient_id, amount, override_authorized, device_info)
            self._set_headers(200)
            self.wfile.write(json.dumps(result).encode("utf-8"))

        elif path == "/api/reset":
            engine.db = engine._get_default_database()
            engine.save_data()
            engine.graph.load_from_db(engine.db)
            self._set_headers(200)
            self.wfile.write(json.dumps({"status": "success", "message": "Database & graph reset to defaults"}).encode("utf-8"))

        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Endpoint not found"}).encode("utf-8"))

    def log_message(self, format, *args):
        sys.stdout.write(f"[Fintech-Risk-Server] {format % args}\n")
        sys.stdout.flush()

def run_server():
    server_address = ("", PORT)
    httpd = HTTPServer(server_address, FintechRequestHandler)
    print("=" * 70)
    print(f"🚀 Advanced Multi-Dimensional Fintech AI Risk Engine Backend on port {PORT}")
    print("=" * 70)
    print("Features Active:")
    print("  • Device Risk & Geolocation Analysis")
    print("  • Merchant Website Legitimacy & SSL Verification")
    print("  • Customer-Device-Beneficiary Graph Mining")
    print("  • Separate Customer Risk & Beneficiary Risk Estimation")
    print("  • Confidence & Uncertainty Scoring")
    print("  • Explainable Reason Codes (RC_...)")
    print("  • Dynamic Trust Loop & Federated Learning (FedAvg)")
    print("=" * 70)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nShutting down server...")
        httpd.server_close()

if __name__ == "__main__":
    run_server()
