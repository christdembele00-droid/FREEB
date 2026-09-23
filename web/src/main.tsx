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

const API_URL=(import.meta.env.VITE_FREEB_API_URL || "https://freeb-api.onrender.com").replace(/\/$/,"");
const firebaseConfig={
  apiKey:import.meta.env.VITE_FIREBASE_API_KEY || "",
  authDomain:import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "",
  projectId:import.meta.env.VITE_FIREBASE_PROJECT_ID || "",
  storageBucket:import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "",
  messagingSenderId:import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "",
  appId:import.meta.env.VITE_FIREBASE_APP_ID || "",
};
const firebaseReady=Object.values(firebaseConfig).every(Boolean);
const firebaseApp=firebaseReady ? (getApps()[0] ?? initializeApp(firebaseConfig)) : null;
const auth=firebaseApp ? getAuth(firebaseApp) : null;
const googleProvider=new GoogleAuthProvider();

type Health={status?:string;database?:string};
type Conversation={id:string;created_at?:string;name?:string;title?:string;last_message?:string};

function initials(value:string){return value.split(/\s+/).map(part=>part[0]).join("").slice(0,2).toUpperCase() || "FB";}
function messageText(value:Record<string,unknown>){
  for(const key of ["body","text","content","message"]){
    if(typeof value[key]==="string" && value[key]) return String(value[key]);
  }
  return "";
}
function messageSender(value:Record<string,unknown>){
  for(const key of ["sender_name","display_name","username"]){
    if(typeof value[key]==="string" && value[key]) return String(value[key]);
  }
  return "FREEB";
}
function messageMine(value:Record<string,unknown>,user:User|null){
  if(!user) return false;
  return ["sender_id","user_id","author_id","from_user_id"].some(key=>String(value[key]??"")===user.uid);
}
function timeLabel(value:unknown){
  if(typeof value!=="string" && typeof value!=="number") return "";
  const date=new Date(value);
  return Number.isNaN(date.getTime()) ? "" : date.toLocaleTimeString([], {hour:"2-digit",minute:"2-digit"});
}
async function tokenFor(user:User|null){if(!user) throw new Error("Connexion requise");return user.getIdToken();}
async function api(user:User,path:string,init:RequestInit={}){
  const token=await tokenFor(user);
  const headers=new Headers(init.headers);
  headers.set("Authorization","Bearer "+token);
  if(init.body && !headers.has("Content-Type")) headers.set("Content-Type","application/json");
  const response=await fetch(API_URL+path,{...init,headers});
  const body=await response.text();
  if(!response.ok) throw new Error(body || ("HTTP "+response.status));
  return body ? JSON.parse(body) : null;
}

function Landing({readyLabel,onLogin}:{readyLabel:string;onLogin:()=>void}){
  const names=["AMANI","MAYA","TEAM F2","KOFFI","CIRCLE"];
  return <main className="landing">
    <section className="landing-card">
      <div className="landing-copy">
        <div className="brand-lockup"><div className="brand-mark">FB</div><div className="brand-text"><strong>FREEB</strong><span>messaging · media · people</span></div></div>
        <div style={{marginTop:44}} className="eyebrow">PRIVATE SOCIAL SPACE</div>
        <h1>Talk. Share.<br/>Stay close.</h1>
        <p>Une interface pensée comme un vrai produit mobile : conversations rapides, présence en ligne et expérience visuelle fluide.</p>
        <div className="landing-actions">
          <button className="btn primary" onClick={onLogin}>Continuer avec Google ↗</button>
          <span className="status-pill online">{readyLabel}</span>
        </div>
        <div className="landing-note">Tes données applicatives passent par le backend FREEB. Firebase gère l'identité et Google Sign-In.</div>
      </div>
      <div className="landing-visual" aria-hidden="true">
        <div className="orb"></div>
        <div className="phone-stage">
          <div className="phone-screen">
            <div className="fake-head"><div className="fake-lines"><i></i><i></i></div><div className="fake-avatar"></div></div>
            <div className="fake-list">
              {names.map((name,index)=><div className="fake-chat" key={name}><span>{initials(name)}</span><div></div>{index<3&&<em/>}</div>)}
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>;
}

function App(){
  const [user,setUser]=useState<User|null>(null);
  const [health,setHealth]=useState<Health|null>(null);
  const [status,setStatus]=useState("Initialisation");
  const [profile,setProfile]=useState<Record<string,unknown>|null>(null);
  const [conversations,setConversations]=useState<Conversation[]>([]);
  const [selectedConversation,setSelectedConversation]=useState<string|null>(null);
  const [messages,setMessages]=useState<Record<string,unknown>[]>([]);
  const [draft,setDraft]=useState("");
  const [socket,setSocket]=useState<WebSocket|null>(null);

  const readyLabel=useMemo(()=>health?.status==="ok" ? "API opérationnelle" : health ? "API à vérifier" : "Connexion API...",[health]);

  useEffect(()=>{if(!auth)return;return onAuthStateChanged(auth,setUser);},[]);
  useEffect(()=>{
    fetch(API_URL+"/v1/health/ready").then(response=>response.json()).then(data=>setHealth(data)).catch(error=>setHealth({status:String(error)}));
  },[]);
  async function loadAccount(nextUser=user){
    if(!nextUser)return;
    setStatus("Synchronisation");
    try{
      const [me,list]=await Promise.all([api(nextUser,"/v1/users/me"),api(nextUser,"/v1/chat/conversations")]);
      setProfile(me);setConversations(Array.isArray(list)?list:[]);setStatus("En ligne");
    }catch(error){setStatus(error instanceof Error?error.message:String(error));}
  }
  useEffect(()=>{void loadAccount();},[user]);

  async function openConversation(id:string){
    if(!user)return;
    setSelectedConversation(id);socket?.close();
    try{
      const data=await api(user,"/v1/chat/conversations/"+id+"/messages");
      setMessages(Array.isArray(data)?data:[]);
      const token=await user.getIdToken();
      const wsBase=API_URL.replace(/^https:/,"wss:").replace(/^http:/,"ws:");
      const ws=new WebSocket(wsBase+"/v1/ws/"+id+"?token="+encodeURIComponent(token));
      ws.onopen=()=>setStatus("Canal en direct");
      ws.onclose=()=>setStatus("Hors ligne");
      ws.onmessage=event=>{
        try{
          const payload=JSON.parse(event.data);
          const item=payload?.data && typeof payload.data==="object" ? payload.data : payload;
          setMessages(current=>[...current,item]);
        }catch{}
      };
      setSocket(ws);
    }catch(error){setStatus(error instanceof Error?error.message:String(error));}
  }

  async function sendMessage(){
    if(!user || !selectedConversation || !draft.trim())return;
    const body=draft.trim();setDraft("");
    try{
      const created=await api(user,"/v1/chat/conversations/"+selectedConversation+"/messages",{method:"POST",body:JSON.stringify({body})});
      setMessages(current=>[...current,created]);
    }catch(error){setStatus(error instanceof Error?error.message:String(error));setDraft(body);}
  }

  async function login(){
    if(!auth)return;
    try{await signInWithPopup(auth,googleProvider);}catch(error){setStatus(error instanceof Error?error.message:"Connexion Google échouée");}
  }

  if(!firebaseReady || !auth){
    return <main className="shell">
      <Landing readyLabel={readyLabel} onLogin={login}/>
      {!firebaseReady && <div className="notice"><div className="eyebrow">SETUP</div><h2>Firebase Web n'est pas actif.</h2><p>Les variables <code>VITE_FIREBASE_*</code> doivent être présentes dans le build Render de <b>freeb-web</b>.</p></div>}
    </main>;
  }

  return <main className="shell">
    <header className="topbar">
      <div className="brand-lockup"><div className="brand-mark">FB</div><div className="brand-text"><strong>FREEB</strong><span>social space</span></div></div>
      <div className="top-actions"><span className={health?.status==="ok" ? "status-pill online" : "status-pill"}>{readyLabel}</span><button className="btn" onClick={()=>void loadAccount()}>Rafraîchir</button><button className="btn icon" onClick={()=>void signOut(auth)} title="Déconnexion">↗</button></div>
    </header>

    <div className="app-grid">
      <aside className="glass sidebar">
        <div className="profile-card">
          {user?.photoURL ? <img className="avatar" src={user.photoURL} alt=""/> : <div className="avatar" style={{display:"grid",placeItems:"center"}}>{initials(user?.displayName || "FREEB")}</div>}
          <div className="profile-copy"><strong>{user?.displayName || "FREEB User"}</strong><span>{user?.email || "Compte Google"}</span></div>
        </div>
        <div className="sidebar-actions"><button className="btn primary">Nouveau</button><button className="btn icon">+</button></div>
        <div className="section-label">Messages</div>
        <div className="conversation-list">
          {conversations.length===0 ? <div className="empty-state">Aucune conversation pour le moment.<br/>Crée ou ouvre une conversation depuis ton compte.</div> :
          conversations.map((item,index)=>{
            const label=item.name || item.title || ("Conversation "+(index+1));
            return <button className={selectedConversation===item.id ? "conversation active" : "conversation"} key={item.id} onClick={()=>void openConversation(item.id)}>
              <span className="avatar-mini">{initials(label)}</span>
              <span className="conversation-main"><strong>{label}</strong><span>{item.last_message || "Ouvrir la conversation"}</span></span>
              <span className="conversation-time">{item.created_at?timeLabel(item.created_at):""}</span>
            </button>;
          })}
        </div>
        <div style={{marginTop:"auto",paddingTop:18}} className="section-label">Compte sécurisé</div>
        <div style={{fontSize:11,color:"#7f8a9b",lineHeight:1.6}}>Firebase Identity<br/>PostgreSQL data layer<br/>WebSocket realtime</div>
      </aside>

      <section className="glass chat-shell">
        <div className="chat-head">
          <div className="chat-title"><span className="dot"></span><div><h2>{selectedConversation ? (conversations.find(item=>item.id===selectedConversation)?.name || conversations.find(item=>item.id===selectedConversation)?.title || "Conversation") : "Tes messages"}</h2><p>{selectedConversation ? status : "Sélectionne une conversation pour commencer."}</p></div></div>
          <button className="btn icon">•••</button>
        </div>
        <div className="messages">
          {!selectedConversation && <div className="empty-state" style={{margin:"15vh auto",maxWidth:360}}><div style={{fontSize:42,marginBottom:14}}>✦</div><strong style={{color:"#eef5ff",fontSize:18}}>Ton espace de discussion</strong><p style={{color:"#768295",lineHeight:1.7}}>Les messages apparaîtront ici avec une présentation claire, des bulles fluides et le temps réel.</p></div>}
          {selectedConversation && messages.map((message,index)=>{
            const text=messageText(message) || "Nouveau message";
            const mine=messageMine(message,user);
            const created=message.created_at ?? message.createdAt ?? message.timestamp;
            return <div className={mine ? "message-row mine" : "message-row"} key={index}><div className="bubble">{!mine&&<div style={{fontSize:9,color:"#71dff0",fontWeight:800,marginBottom:4}}>{messageSender(message)}</div>}<div>{text}</div><small>{timeLabel(created) || "maintenant"}</small></div></div>;
          })}
        </div>
        {selectedConversation && <form className="composer" onSubmit={event=>{event.preventDefault();void sendMessage();}}><button type="button" className="btn icon" aria-label="Ajouter">+</button><input value={draft} onChange={event=>setDraft(event.target.value)} placeholder="Écrire quelque chose..."/><button className="btn primary send" type="submit">Envoyer</button></form>}
      </section>

      <aside className="glass account">
        <div className="account-hero"><div className="eyebrow">PROFILE</div><h2>{user?.displayName || "FREEB User"}</h2><p>{user?.email || "Compte Google connecté"}</p></div>
        <div className="stat-grid"><div className="stat"><strong>{conversations.length}</strong><span>conversations</span></div><div className="stat"><strong>{messages.length}</strong><span>messages ouverts</span></div><div className="stat"><strong>ON</strong><span>realtime</span></div><div className="stat"><strong>SSL</strong><span>transport</span></div></div>
        <div className="section-label" style={{marginTop:22}}>Session</div>
        <div className="info-list"><div className="info-row"><span>Statut</span><b style={{color:"#7df0bd"}}>{status}</b></div><div className="info-row"><span>Projet</span><b>freeb-bfa38</b></div><div className="info-row"><span>Backend</span><b>Render · Frankfurt</b></div><div className="info-row"><span>Identité</span><b>Google / Firebase</b></div></div>
      </aside>
    </div>
  </main>;
}

createRoot(document.getElementById("root")!).render(<React.StrictMode><App/></React.StrictMode>);
