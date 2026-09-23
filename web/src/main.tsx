import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  GoogleAuthProvider,
  User,
  getAuth,
  onAuthStateChanged,
  signInWithPopup,
  signOut,
} from "firebase/auth";
import { initializeApp, getApps } from "firebase/app";
import "./styles.css";

const API_URL = (import.meta.env.VITE_FREEB_API_URL || "https://freeb-api.onrender.com").replace(/\/$/, "");

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "",
};

const firebaseReady = Object.values(firebaseConfig).every(Boolean);
const firebaseApp = firebaseReady
  ? (getApps()[0] ?? initializeApp(firebaseConfig))
  : null;
const auth = firebaseApp ? getAuth(firebaseApp) : null;

type Health = { status?: string; database?: string };
type Conversation = { id: string; created_at?: string };

async function tokenFor(user: User | null): Promise<string> {
  if (!user) throw new Error("Connexion requise");
  return user.getIdToken();
}

async function api(user: User, path: string, init: RequestInit = {}) {
  const token = await tokenFor(user);
  const headers = new Headers(init.headers);
  headers.set("Authorization", "Bearer " + token);
  if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");
  const response = await fetch(API_URL + path, { ...init, headers });
  const body = await response.text();
  if (!response.ok) throw new Error(`HTTP ${response.status}: ${body}`);
  return body ? JSON.parse(body) : null;
}

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [health, setHealth] = useState<Health | null>(null);
  const [status, setStatus] = useState("Prêt");
  const [profile, setProfile] = useState<Record<string, unknown> | null>(null);
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<string | null>(null);
  const [messages, setMessages] = useState<Record<string, unknown>[]>([]);
  const [draft, setDraft] = useState("");
  const [socket, setSocket] = useState<WebSocket | null>(null);

  useEffect(() => {
    if (!auth) return;
    return onAuthStateChanged(auth, setUser);
  }, []);

  useEffect(() => {
    fetch(API_URL + "/v1/health/ready")
      .then((r) => r.json())
      .then((data) => setHealth(data))
      .catch((error) => setHealth({ status: String(error) }));
  }, []);

  async function loadAccount(nextUser = user) {
    if (!nextUser) return;
    setStatus("Chargement...");
    try {
      const [me, list] = await Promise.all([
        api(nextUser, "/v1/users/me"),
        api(nextUser, "/v1/chat/conversations"),
      ]);
      setProfile(me);
      setConversations(Array.isArray(list) ? list : []);
      setStatus("Connecté");
    } catch (error) {
      setStatus(String(error));
    }
  }

  useEffect(() => {
    void loadAccount();
  }, [user]);

  async function openConversation(id: string) {
    if (!user) return;
    setSelectedConversation(id);
    try {
      const data = await api(user, "/v1/chat/conversations/" + id + "/messages");
      setMessages(Array.isArray(data) ? data : []);
      socket?.close();

      const token = await user.getIdToken();
      const wsBase = API_URL.replace(/^https:/, "wss:").replace(/^http:/, "ws:");
      const ws = new WebSocket(`${wsBase}/v1/ws/${id}?token=${encodeURIComponent(token)}`);
      ws.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data);
          setMessages((current) => [...current, payload]);
        } catch {
          // Ignore malformed socket frames.
        }
      };
      setSocket(ws);
    } catch (error) {
      setStatus(String(error));
    }
  }

  async function sendMessage() {
    if (!user || !selectedConversation || !draft.trim()) return;
    const body = draft.trim();
    setDraft("");
    try {
      const created = await api(user, "/v1/chat/conversations/" + selectedConversation + "/messages", {
        method: "POST",
        body: JSON.stringify({ body }),
      });
      setMessages((current) => [...current, created]);
    } catch (error) {
      setStatus(String(error));
      setDraft(body);
    }
  }

  const readyLabel = useMemo(() => {
    if (!health) return "Vérification...";
    return health.status === "ok" ? "API opérationnelle" : "API à vérifier";
  }, [health]);

  if (!firebaseReady) {
    return (
      <main className="shell">
        <section className="hero card">
          <span className="eyebrow">FREEB WEB</span>
          <h1>Le site est en ligne.</h1>
          <p>{readyLabel}. Le backend partagé avec l’APK est <b>{API_URL}</b>.</p>
          <div className="notice">
            Firebase Web n’est pas encore configuré. Ajoute les variables VITE_FIREBASE_* dans Render
            pour activer la connexion Google et les fonctions protégées.
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="shell">
      <header className="topbar">
        <div>
          <span className="eyebrow">FREEB</span>
          <strong>Web + Android</strong>
        </div>
        <div className="top-actions">
          <span className={health?.status === "ok" ? "pill online" : "pill"}>{readyLabel}</span>
          {user ? (
            <button onClick={() => signOut(auth!)}>Déconnexion</button>
          ) : (
            <button onClick={() => signInWithPopup(auth!, new GoogleAuthProvider())}>Continuer avec Google</button>
          )}
        </div>
      </header>

      {!user ? (
        <section className="hero card">
          <span className="eyebrow">SOCIAL PLATFORM</span>
          <h1>FREEB sur le Web.</h1>
          <p>Le site utilise le même backend Render, Firebase et PostgreSQL que l’application Android.</p>
          <button className="primary" onClick={() => signInWithPopup(auth!, new GoogleAuthProvider())}>
            Se connecter
          </button>
        </section>
      ) : (
        <div className="grid">
          <aside className="card sidebar">
            <div className="profile">
              {user.photoURL && <img src={user.photoURL} alt="" />}
              <div>
                <strong>{user.displayName || "FREEB User"}</strong>
                <span>{user.email}</span>
              </div>
            </div>
            <button className="ghost" onClick={() => void loadAccount()}>Actualiser</button>
            <h3>Conversations</h3>
            {conversations.length === 0 ? (
              <p className="muted">Aucune conversation.</p>
            ) : conversations.map((item) => (
              <button
                className={selectedConversation === item.id ? "conversation active" : "conversation"}
                key={item.id}
                onClick={() => void openConversation(item.id)}
              >
                <span>Conversation</span>
                <small>{item.id.slice(0, 8)}</small>
              </button>
            ))}
          </aside>

          <section className="card chat">
            <div className="chat-head">
              <div>
                <span className="eyebrow">MESSAGES</span>
                <h2>{selectedConversation || "Sélectionne une conversation"}</h2>
              </div>
              <span className="pill">{status}</span>
            </div>
            <div className="messages">
              {messages.map((message, index) => (
                <article className="message" key={index}>
                  <pre>{JSON.stringify(message, null, 2)}</pre>
                </article>
              ))}
            </div>
            {selectedConversation && (
              <form
                className="composer"
                onSubmit={(event) => {
                  event.preventDefault();
                  void sendMessage();
                }}
              >
                <input value={draft} onChange={(e) => setDraft(e.target.value)} placeholder="Écrire un message..." />
                <button className="primary" type="submit">Envoyer</button>
              </form>
            )}
          </section>

          <aside className="card account">
            <span className="eyebrow">COMPTE</span>
            <h2>Profil</h2>
            <pre>{profile ? JSON.stringify(profile, null, 2) : "Chargement..."}</pre>
          </aside>
        </div>
      )}
    </main>
  );
}

createRoot(document.getElementById("root")!).render(
  <React.StrictMode><App /></React.StrictMode>
);
