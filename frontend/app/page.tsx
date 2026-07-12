"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";

type Movie = {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  genre: string;
  ageRestriction: number;
  releaseDate?: string;
  posterUrl?: string;
  status: "ACTIVE" | "UPCOMING" | "INACTIVE";
};

type Reservation = {
  id: number;
  reservationCode: string;
  status: string;
  totalPrice: number;
  screeningId: number;
  expiresAt: string;
};

type Ticket = {
  id: number;
  ticketCode: string;
  createdAt: string;
  reservationId: number;
};

type Screening = {
  id: number;
  startTime: string;
  endTime: string;
  price: number;
  status: "ACTIVE" | "CANCELLED" | "COMPLETED";
  movieId: number;
  hallId: number;
};

type Seat = { id: number; rowNumber: number; seatNumber: number; seatType: string; hallId: number };
type Hall = { id: number; name: string; totalRows: number; seatsPerRow: number };
type ReservationSeat = { id: number; price: number; reservationId: number; seatId: number; screeningId: number };
type UserProfile = { id: number; username: string; email: string; role: string; createdAt: string };

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "/backend-api";

const demoMovies: Movie[] = [
  { id: 1, title: "Dune: Çöl Gezegeni", description: "İmparatorluğun kaderini değiştirecek destansı bir yolculuk.", durationMinutes: 166, genre: "Bilim Kurgu", ageRestriction: 13, status: "ACTIVE" },
  { id: 2, title: "Gece Yarısı", description: "İstanbul sokaklarında geçen sürükleyici bir suç hikâyesi.", durationMinutes: 118, genre: "Gerilim", ageRestriction: 16, status: "ACTIVE" },
  { id: 3, title: "Son Perde", description: "Bir tiyatro topluluğunun umut ve dostlukla örülü hikâyesi.", durationMinutes: 104, genre: "Dram", ageRestriction: 7, status: "ACTIVE" },
  { id: 4, title: "Atlas'ın Sırrı", description: "Kayıp bir haritanın peşinde kıtaları aşan bir macera.", durationMinutes: 132, genre: "Macera", ageRestriction: 10, status: "UPCOMING" },
];

const seatRows = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];

async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || "İşlem tamamlanamadı");
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}

export default function Home() {
  const [activeNav, setActiveNav] = useState("Keşfet");
  const [movies, setMovies] = useState<Movie[]>(demoMovies);
  const [search, setSearch] = useState("");
  const [authOpen, setAuthOpen] = useState(false);
  const [seatOpen, setSeatOpen] = useState(false);
  const [selectedMovie, setSelectedMovie] = useState<Movie>(demoMovies[0]);
  const [selectedSeats, setSelectedSeats] = useState<number[]>([]);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [screenings, setScreenings] = useState<Screening[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [halls, setHalls] = useState<Hall[]>([]);
  const [reservationSeats, setReservationSeats] = useState<ReservationSeat[]>([]);
  const [occupiedSeatIds, setOccupiedSeatIds] = useState<number[]>([]);
  const [selectedScreening, setSelectedScreening] = useState<Screening | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [profileModal, setProfileModal] = useState<"username" | "email" | "password" | null>(null);
  const [toast, setToast] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    setIsLoggedIn(Boolean(localStorage.getItem("accessToken")));
    apiRequest<{ content: Movie[] }>("/api/movies?size=12")
      .then((data) => data.content?.length && setMovies(data.content))
      .catch(() => undefined);
    apiRequest<{ content: Screening[] }>("/api/screenings?size=100")
      .then((data) => setScreenings(data.content || []))
      .catch(() => undefined);
    apiRequest<{ content: Seat[] }>("/api/seats?size=300")
      .then((data) => setSeats(data.content || []))
      .catch(() => undefined);
    apiRequest<{ content: Hall[] }>("/api/halls?size=100")
      .then((data) => setHalls(data.content || []))
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    if (!isLoggedIn) return;
    loadReservationData().catch(() => undefined);
    loadProfile();
  }, [isLoggedIn]);

  async function loadReservationData() {
    const [reservationData, ticketData, reservationSeatData] = await Promise.all([
      apiRequest<{ content: Reservation[] }>("/api/reservations?size=100"),
      apiRequest<{ content: Ticket[] }>("/api/tickets?size=100"),
      apiRequest<{ content: ReservationSeat[] }>("/api/reservation-seats?size=500"),
    ]);
    setReservations(reservationData.content || []);
    setTickets(ticketData.content || []);
    setReservationSeats(reservationSeatData.content || []);
  }

  async function loadProfile() {
    try { setProfile(await apiRequest<UserProfile>("/api/users/me")); }
    catch { setProfile(null); }
  }

  const filteredMovies = useMemo(() => movies.filter((movie) =>
    `${movie.title} ${movie.genre}`.toLocaleLowerCase("tr").includes(search.toLocaleLowerCase("tr"))
  ), [movies, search]);

  function showToast(message: string) {
    setToast(message);
    window.setTimeout(() => setToast(""), 3200);
  }

  function openSeats(movie: Movie, screening?: Screening) {
    const futureScreenings = screenings
      .filter((item) => item.movieId === movie.id && item.status === "ACTIVE" && new Date(item.startTime).getTime() > Date.now())
      .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime());
    const nextScreening = screening || futureScreenings[0];
    if (!nextScreening) {
      showToast("Bu film için gelecekte aktif bir seans bulunmuyor.");
      return;
    }
    setSelectedMovie(movie);
    setSelectedScreening(nextScreening);
    setSelectedSeats([]);
    setOccupiedSeatIds([]);
    setSeatOpen(true);
    apiRequest<number[]>(`/api/screenings/${nextScreening.id}/occupied-seat-ids`)
      .then(setOccupiedSeatIds)
      .catch(() => setOccupiedSeatIds([]));
  }

  function toggleSeat(seatId: number) {
    if (occupiedSeatIds.includes(seatId)) return;
    setSelectedSeats((current) => current.includes(seatId)
      ? current.filter((id) => id !== seatId)
      : [...current, seatId]
    );
  }

  async function createReservation() {
    if (!isLoggedIn) {
      setSeatOpen(false);
      setAuthOpen(true);
      return;
    }
    if (!selectedSeats.length) return showToast("Lütfen en az bir koltuk seçin.");
    try {
      await apiRequest("/api/reservations", {
        method: "POST",
        body: JSON.stringify({ screeningId: selectedScreening?.id, seatIds: selectedSeats }),
      });
      setOccupiedSeatIds((current) => [...new Set([...current, ...selectedSeats])]);
      await loadReservationData();
      setSeatOpen(false);
      setActiveNav("Biletlerim");
      showToast("Rezervasyonunuz oluşturuldu.");
    } catch (error) {
      showToast(error instanceof Error ? error.message : "Rezervasyon oluşturulamadı");
    }
  }

  const navItems = profile?.role === "ROLE_ADMIN"
    ? ["Keşfet", "Filmler", "Biletlerim", "Profil", "Yönetim"]
    : ["Keşfet", "Filmler", "Biletlerim", "Profil"];

  const renderExplore = () => (
    <>
      <section className="hero">
        <div className="hero-glow" />
        <div className="hero-copy">
          <span className="eyebrow"><i /> Bu haftanın seçkisi</span>
          <h1>Perde açılıyor.<br /><em>Hikâyeni seç.</em></h1>
          <p>Şehrin en iyi salonlarında, en yeni filmleri keşfet. Koltuğunu seç, biletin saniyeler içinde cebinde olsun.</p>
          <div className="hero-actions">
            <button className="primary" onClick={() => movies[0] && openSeats(movies[0])}>Biletini al <span>→</span></button>
            <button className="ghost" onClick={() => setActiveNav("Filmler")}>Tüm filmler</button>
          </div>
        </div>
        <div className="hero-card" aria-hidden="true">
          <div className="poster-number">01</div>
          <div className="poster-lines"><span /><span /><span /></div>
          <div className="poster-title">DUNE</div>
          <div className="poster-sub">ÇÖL GEZEGENİ</div>
        </div>
        <div className="hero-meta">
          <span>IMAX</span><span>166 dk</span><span>13+</span>
        </div>
      </section>

      <section className="metrics">
        <div><strong>12</strong><span>vizyondaki film</span></div>
        <div><strong>48</strong><span>günlük seans</span></div>
        <div><strong>4.9</strong><span>izleyici puanı</span></div>
        <div className="metric-cta"><span>Sıradaki seans</span><b>{nextScreeningLabel(screenings)}</b></div>
      </section>

      <MovieSection title="Vizyondakiler" subtitle="Bu hafta büyük perdede" movies={filteredMovies.slice(0, 4)} screenings={screenings} onSelect={openSeats} />

      <section className="experience">
        <div className="experience-visual"><div className="screen-beam" /><span>CINEMA<br />EXPERIENCE</span></div>
        <div className="experience-copy">
          <span className="section-kicker">Sıradan bir gösterim değil</span>
          <h2>Her detayda<br />daha iyi sinema.</h2>
          <div className="feature-list">
            <div><b>01</b><span><strong>Kristal netlik</strong>4K lazer projeksiyon</span></div>
            <div><b>02</b><span><strong>İçinde hisset</strong>Dolby Atmos ses sistemi</span></div>
            <div><b>03</b><span><strong>Rahatça izle</strong>Premium geniş koltuklar</span></div>
          </div>
        </div>
      </section>
    </>
  );

  return (
    <main>
      <header>
        <button className="brand" onClick={() => setActiveNav("Keşfet")}><span>C</span>CINEMA</button>
        <nav>{navItems.map((item) => <button key={item} className={activeNav === item ? "active" : ""} onClick={() => setActiveNav(item)}>{item}</button>)}</nav>
        <div className="header-actions">
          <label className="search"><span>⌕</span><input aria-label="Film ara" placeholder="Film ara" value={search} onChange={(event) => setSearch(event.target.value)} /></label>
          <button className="account" onClick={() => isLoggedIn ? setActiveNav("Profil") : setAuthOpen(true)}>{isLoggedIn ? "Hesabım" : "Giriş yap"}</button>
        </div>
      </header>

      {activeNav === "Keşfet" && renderExplore()}
      {activeNav === "Filmler" && <div className="page-shell"><PageHeading eyebrow="Program" title="Vizyondaki filmler" text="Tarzını seç, seansını bul ve en iyi koltuğu ayırt." /><MovieSection movies={filteredMovies} screenings={screenings} onSelect={openSeats} /></div>}
      {activeNav === "Biletlerim" && <TicketsView tickets={tickets} reservations={reservations} reservationSeats={reservationSeats} movies={movies} screenings={screenings} seats={seats} halls={halls} loggedIn={isLoggedIn} onLogin={() => setAuthOpen(true)} />}
      {activeNav === "Profil" && <ProfileView profile={profile} loggedIn={isLoggedIn} onLogin={() => setAuthOpen(true)} onEdit={setProfileModal} onLogout={() => { localStorage.clear(); setIsLoggedIn(false); setProfile(null); setActiveNav("Keşfet"); showToast("Oturum kapatıldı."); }} />}
      {activeNav === "Yönetim" && profile?.role === "ROLE_ADMIN" && <><AdminPanel movies={movies} halls={halls} screenings={screenings} seats={seats} onChanged={() => window.location.reload()} showToast={showToast} /><ScreeningDeletePanel movies={movies} screenings={screenings} onChanged={() => window.location.reload()} showToast={showToast} /></>}

      <footer><div className="brand footer-brand"><span>C</span>CINEMA</div><p>Sinemanın en iyi hâli, tek ekranda.</p><div><span>© 2026 Cinema</span><a href="#">Gizlilik</a><a href="#">Yardım</a></div></footer>

      {authOpen && <AuthModal onClose={() => setAuthOpen(false)} onSuccess={() => { setIsLoggedIn(true); setAuthOpen(false); showToast("Hoş geldiniz."); }} />}
      {profileModal && <ProfileModal mode={profileModal} onClose={() => setProfileModal(null)} onSuccess={() => {
        const requiresLogin = profileModal === "email" || profileModal === "password";
        setProfileModal(null);
        if (requiresLogin) {
          localStorage.clear(); setIsLoggedIn(false); setProfile(null);
          showToast(profileModal === "email" ? "E-postanı doğruladıktan sonra tekrar giriş yap." : "Şifren değişti. Lütfen tekrar giriş yap.");
        } else { loadProfile(); showToast("Bilgileriniz güncellendi."); }
      }} />}
      {seatOpen && selectedScreening && <SeatModal movie={selectedMovie} screening={selectedScreening} seats={seats.filter((seat) => seat.hallId === selectedScreening.hallId)} selected={selectedSeats} occupied={occupiedSeatIds} onToggle={toggleSeat} onClose={() => setSeatOpen(false)} onContinue={createReservation} />}
      {toast && <div className="toast">✓ {toast}</div>}
    </main>
  );
}

function PageHeading({ eyebrow, title, text }: { eyebrow: string; title: string; text: string }) {
  return <div className="page-heading"><span>{eyebrow}</span><h1>{title}</h1><p>{text}</p></div>;
}

function formatScreeningDate(value: string) {
  return new Intl.DateTimeFormat("tr-TR", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }).format(new Date(value));
}

function nextScreeningLabel(screenings: Screening[]) {
  const next = screenings.filter((item) => item.status === "ACTIVE" && new Date(item.startTime).getTime() > Date.now())
    .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0];
  return next ? formatScreeningDate(next.startTime) : "Program hazırlanıyor";
}

function MovieSection({ title, subtitle, movies, screenings, onSelect }: { title?: string; subtitle?: string; movies: Movie[]; screenings: Screening[]; onSelect: (movie: Movie, screening?: Screening) => void }) {
  return <section className="movies-section">
    {title && <div className="section-heading"><div><span>{subtitle}</span><h2>{title}</h2></div><button>Tümünü gör →</button></div>}
    <div className="movie-grid">{movies.map((movie, index) => { const movieScreenings = screenings.filter((item) => item.movieId === movie.id && item.status === "ACTIVE" && new Date(item.startTime).getTime() > Date.now()).sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()).slice(0, 3); return <article className={`movie-card tone-${index % 4}`} key={movie.id}>
      <div className="movie-poster"><div className="movie-index">0{index + 1}</div><div className="movie-art">{movie.title.split(" ")[0]}</div><span className="rating">★ 4.{9 - index}</span><button aria-label={`${movie.title} için bilet al`} onClick={() => onSelect(movie)}>Bilet al</button></div>
      <div className="movie-info"><span>{movie.genre} · {movie.durationMinutes} dk</span><h3>{movie.title}</h3><p>{movie.description}</p><div className="showtimes">{movieScreenings.length ? movieScreenings.map((screening) => <button key={screening.id} onClick={() => onSelect(movie, screening)}>{formatScreeningDate(screening.startTime)}</button>) : <small>Gelecek seans bulunmuyor</small>}</div></div>
    </article>})}</div>
  </section>;
}

function TicketsView({ tickets, reservations, reservationSeats, movies, screenings, seats, halls, loggedIn, onLogin }: { tickets: Ticket[]; reservations: Reservation[]; reservationSeats: ReservationSeat[]; movies: Movie[]; screenings: Screening[]; seats: Seat[]; halls: Hall[]; loggedIn: boolean; onLogin: () => void }) {
  if (!loggedIn) return <EmptyState title="Biletlerin burada" text="Rezervasyonlarını ve dijital biletlerini görüntülemek için giriş yap." action="Giriş yap" onAction={onLogin} />;
  const rows: Array<{ key: string; code: string; movie: string; detail: string }> = [];
  for (const reservation of reservations) {
    const ticket = tickets.find((item) => item.reservationId === reservation.id);
    const screening = screenings.find((item) => item.id === reservation.screeningId);
    const movie = movies.find((item) => item.id === screening?.movieId);
    const hall = halls.find((item) => item.id === screening?.hallId);
    const links = reservationSeats.filter((item) => item.reservationId === reservation.id);
    const displayCode = ticket?.ticketCode || reservation.reservationCode;
    if (links.length === 0) {
      rows.push({ key: `reservation-${reservation.id}`, code: displayCode, movie: movie?.title || "Film bilgisi", detail: `${hall?.name || "Salon"} · ${screening ? formatScreeningDate(screening.startTime) : "Seans bilgisi"} · ${ticket ? "Bilet hazır" : "Rezervasyon oluşturuldu"}` });
    } else {
      for (const link of links) {
        const seat = seats.find((item) => item.id === link.seatId);
        const seatLabel = seat ? `${seatRows[seat.rowNumber - 1] || seat.rowNumber}${seat.seatNumber}` : `#${link.seatId}`;
        rows.push({ key: `reservation-${reservation.id}-${link.id}`, code: `${displayCode}-${seatLabel}`, movie: movie?.title || "Film bilgisi", detail: `${hall?.name || `Salon ${screening?.hallId || ""}`} · ${screening ? formatScreeningDate(screening.startTime) : "Seans bilgisi"} · Koltuk ${seatLabel}` });
      }
    }
  }
  return <div className="page-shell"><PageHeading eyebrow="Cüzdan" title="Biletlerim" text="Ayırttığın her koltuk ayrı bir dijital bilet olarak burada görünür." />{rows.length ? <div className="ticket-grid single-column">{rows.map((row) => <article className="ticket" key={row.key}><div><span>DİJİTAL BİLET</span><h3>{row.movie}</h3><p>{row.detail}</p></div><div className="ticket-code"><i /><i /><i /><i /><i /><strong>{row.code}</strong></div></article>)}</div> : <div className="no-tickets"><h3>Henüz biletin yok</h3><p>Oluşturduğun rezervasyonlar burada görünecek.</p></div>}</div>;
}

function ProfileView({ profile, loggedIn, onLogin, onEdit, onLogout }: { profile: UserProfile | null; loggedIn: boolean; onLogin: () => void; onEdit: (mode: "username" | "email" | "password") => void; onLogout: () => void }) {
  if (!loggedIn) return <EmptyState title="Sana özel bir deneyim" text="Biletlerini yönetmek ve hızlı rezervasyon yapmak için hesabına giriş yap." action="Giriş yap" onAction={onLogin} />;
  return <div className="page-shell profile"><PageHeading eyebrow="Hesabım" title="Profil" text="Kişisel bilgilerini ve güvenlik tercihlerini yönet." /><div className="profile-grid"><section><span className="avatar">{profile?.username?.slice(0, 2).toUpperCase() || "U"}</span><h3>{profile?.username || "Kullanıcı"}</h3><p>{profile?.email || "Profil yükleniyor..."}</p><button className="ghost light" onClick={() => onEdit("username")}>Kullanıcı adını düzenle</button><button className="text-action" onClick={() => onEdit("email")}>E-posta adresini değiştir</button></section><section><h3>Hesap güvenliği</h3><div className="setting"><span>Şifre</span><b>••••••••</b><button onClick={() => onEdit("password")}>Değiştir</button></div><div className="setting"><span>Hesap rolü</span><b className="success">{profile?.role === "ROLE_ADMIN" ? "Yönetici" : "Kullanıcı"}</b></div><button className="danger" onClick={onLogout}>Oturumu kapat</button></section></div></div>;
}

function EmptyState({ title, text, action, onAction }: { title: string; text: string; action: string; onAction: () => void }) {
  return <div className="empty-state"><span className="empty-icon">C</span><h2>{title}</h2><p>{text}</p><button className="primary" onClick={onAction}>{action} →</button></div>;
}

function AuthModal({ onClose, onSuccess }: { onClose: () => void; onSuccess: () => void }) {
  const [mode, setMode] = useState<"login" | "register">("login");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(""); setLoading(true);
    const data = Object.fromEntries(new FormData(event.currentTarget));
    try {
      const result = await apiRequest<{ accessToken?: string; refreshToken?: string }>(`/api/auth/${mode}`, { method: "POST", body: JSON.stringify(data) });
      if (mode === "register") { setMode("login"); setError("Kayıt tamamlandı. E-postanızı doğruladıktan sonra giriş yapın."); }
      else { if (result.accessToken) localStorage.setItem("accessToken", result.accessToken); if (result.refreshToken) localStorage.setItem("refreshToken", result.refreshToken); onSuccess(); }
    } catch (err) { setError(err instanceof Error ? err.message : "İşlem başarısız"); } finally { setLoading(false); }
  }
  return <div className="modal-backdrop" role="presentation"><div className="auth-modal" role="dialog" aria-modal="true" aria-label="Hesap girişi"><button className="modal-close" onClick={onClose}>×</button><div className="brand modal-brand"><span>C</span>CINEMA</div><h2>{mode === "login" ? "Tekrar hoş geldin" : "Sinemaya katıl"}</h2><p>{mode === "login" ? "Biletlerine ve rezervasyonlarına devam et." : "Bir dakikada hesabını oluştur."}</p><form onSubmit={submit}>{mode === "register" && <label>Kullanıcı adı<input name="username" minLength={3} required placeholder="kullanıcı adın" /></label>}<label>E-posta<input name="email" type="email" required placeholder="ornek@mail.com" /></label><label>Şifre<input name="password" type="password" minLength={8} required placeholder="en az 8 karakter" /></label>{error && <div className="form-message">{error}</div>}<button className="primary auth-submit" disabled={loading}>{loading ? "Bekleyin..." : mode === "login" ? "Giriş yap" : "Hesap oluştur"}</button></form><button className="mode-switch" onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>{mode === "login" ? "Hesabın yok mu? Kayıt ol" : "Zaten hesabın var mı? Giriş yap"}</button></div></div>;
}

function ProfileModal({ mode, onClose, onSuccess }: { mode: "username" | "email" | "password"; onClose: () => void; onSuccess: () => void }) {
  const [error, setError] = useState("");
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    const path = mode === "username" ? "/api/users/me/username" : mode === "email" ? "/api/users/me/email" : "/api/users/me/password";
    try { await apiRequest(path, { method: "PATCH", body: JSON.stringify(data) }); onSuccess(); }
    catch (err) { setError(err instanceof Error ? err.message : "Güncelleme yapılamadı"); }
  }
  return <div className="modal-backdrop"><div className="auth-modal"><button className="modal-close" onClick={onClose}>×</button><h2>{mode === "username" ? "Kullanıcı adını değiştir" : mode === "email" ? "E-postanı değiştir" : "Şifreni değiştir"}</h2><p>Değişikliği güvenli şekilde hesabına uygula.</p><form onSubmit={submit}>{mode === "username" && <label>Yeni kullanıcı adı<input name="username" minLength={3} required /></label>}{mode === "email" && <><label>Yeni e-posta<input name="newEmail" type="email" required /></label><label>Mevcut şifre<input name="currentPassword" type="password" minLength={8} required /></label></>}{mode === "password" && <><label>Mevcut şifre<input name="currentPassword" type="password" minLength={8} required /></label><label>Yeni şifre<input name="newPassword" type="password" minLength={8} required /></label></>}{error && <div className="form-message">{error}</div>}<button className="primary auth-submit">Kaydet</button></form></div></div>;
}

function AdminPanel({ movies, halls, screenings, seats, onChanged, showToast }: { movies: Movie[]; halls: Hall[]; screenings: Screening[]; seats: Seat[]; onChanged: () => void; showToast: (message: string) => void }) {
  const [section, setSection] = useState<"movie" | "hall" | "screening" | "seat">("movie");
  const [selectedHallId, setSelectedHallId] = useState<number>(halls[0]?.id || 0);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const raw = Object.fromEntries(new FormData(event.currentTarget));
    let path = "/api/admin/movies"; let payload: Record<string, unknown> = raw;
    if (section === "movie") payload = { ...raw, durationMinutes: Number(raw.durationMinutes), ageRestriction: Number(raw.ageRestriction), status: "ACTIVE" };
    if (section === "hall") { path = "/api/admin/halls"; payload = { name: raw.name, totalRows: 10, seatsPerRow: 10 }; }
    if (section === "screening") { path = "/api/admin/screenings"; payload = { movieId: Number(raw.movieId), hallId: Number(raw.hallId), startTime: raw.startTime, endTime: raw.endTime, price: Number(raw.price), status: "ACTIVE" }; }
    if (section === "seat") return;
    try { await apiRequest(path, { method: "POST", body: JSON.stringify(payload) }); showToast("Kayıt başarıyla oluşturuldu."); window.setTimeout(onChanged, 500); }
    catch (err) { showToast(err instanceof Error ? err.message : "Kayıt oluşturulamadı"); }
  }
  async function removeMovie(id: number) { try { await apiRequest(`/api/admin/movies/${id}`, { method: "DELETE" }); showToast("Film silindi."); window.setTimeout(onChanged, 500); } catch (err) { showToast(err instanceof Error ? err.message : "Film silinemedi"); } }
  async function generateSeats(hallId: number) { try { await apiRequest(`/api/admin/halls/${hallId}/generate-seats`, { method: "POST" }); showToast("Eksik koltuklar standart olarak oluşturuldu."); window.setTimeout(onChanged, 500); } catch (err) { showToast(err instanceof Error ? err.message : "Koltuklar oluşturulamadı"); } }
  async function changeSeatType(seat: Seat, seatType: string) { try { await apiRequest(`/api/admin/seats/${seat.id}`, { method: "PUT", body: JSON.stringify({ hallId: seat.hallId, rowNumber: seat.rowNumber, seatNumber: seat.seatNumber, seatType }) }); showToast(`${seatRows[seat.rowNumber - 1] || seat.rowNumber}${seat.seatNumber} koltuğu güncellendi.`); } catch (err) { showToast(err instanceof Error ? err.message : "Koltuk güncellenemedi"); } }
  const hallSeats = seats.filter((seat) => seat.hallId === selectedHallId).sort((a, b) => a.rowNumber - b.rowNumber || a.seatNumber - b.seatNumber);
  return <div className="page-shell admin-page"><PageHeading eyebrow="Yönetim" title="Admin paneli" text="Filmleri, salonları, seansları ve koltukları yönet." /><div className="admin-stats"><div><strong>{movies.length}</strong><span>Film</span></div><div><strong>{halls.length}</strong><span>Salon</span></div><div><strong>{screenings.length}</strong><span>Seans</span></div></div><div className="admin-layout"><aside>{(["movie","hall","screening","seat"] as const).map((item) => <button key={item} className={section === item ? "active" : ""} onClick={() => setSection(item)}>{item === "movie" ? "Film ekle" : item === "hall" ? "Salon ekle" : item === "screening" ? "Seans ekle" : "Koltuk tipleri"}</button>)}</aside><section className={`admin-form ${section === "seat" ? "seat-manager" : ""}`}><h3>{section === "movie" ? "Yeni film" : section === "hall" ? "Yeni salon" : section === "screening" ? "Yeni seans" : "Koltuk tipleri"}</h3>{section === "seat" ? <><select value={selectedHallId} onChange={(event) => setSelectedHallId(Number(event.target.value))}><option value={0}>Salon seç</option>{halls.map((hall) => <option key={hall.id} value={hall.id}>{hall.name}</option>)}</select><p className="manager-note">Salon oluşturulunca 100 standart koltuk otomatik gelir. Değiştirmek istediğin koltuğun tipini seç.</p><div className="seat-type-grid">{hallSeats.map((seat) => <label key={seat.id}><b>{seatRows[seat.rowNumber - 1] || seat.rowNumber}{seat.seatNumber}</b><select defaultValue={seat.seatType} onChange={(event) => changeSeatType(seat, event.target.value)}><option value="STANDARD">Standart</option><option value="PREMIUM">Premium</option><option value="VIP">VIP</option></select></label>)}</div></> : <form onSubmit={submit}>{section === "movie" && <><input name="title" placeholder="Film adı" required /><textarea name="description" placeholder="Açıklama" required /><div className="form-row"><input name="durationMinutes" type="number" placeholder="Süre (dk)" required /><input name="genre" placeholder="Tür" required /></div><div className="form-row"><input name="ageRestriction" type="number" placeholder="Yaş sınırı" required /><input name="releaseDate" type="date" required /></div><input name="posterUrl" placeholder="Poster URL (opsiyonel)" /></>}{section === "hall" && <><input name="name" placeholder="Salon adı" required /><div className="auto-seat-info"><strong>100 koltuk otomatik oluşturulur</strong><span>10 sıra × her sırada 10 standart koltuk</span></div></>}{section === "screening" && <><select name="movieId" required><option value="">Film seç</option>{movies.map((movie) => <option key={movie.id} value={movie.id}>{movie.title}</option>)}</select><select name="hallId" required><option value="">Salon seç</option>{halls.map((hall) => <option key={hall.id} value={hall.id}>{hall.name}</option>)}</select><div className="form-row"><input name="startTime" type="datetime-local" required /><input name="endTime" type="datetime-local" required /></div><input name="price" type="number" placeholder="Fiyat" required /></>}<button className="primary">Kaydet</button></form>}</section><section className="admin-list"><h3>{section === "hall" ? "Salonlar" : "Filmler"}</h3>{section === "hall" ? halls.map((hall) => <div key={hall.id}><span><b>{hall.name}</b><small>{hall.totalRows} sıra · {hall.seatsPerRow} koltuk</small></span><button onClick={() => generateSeats(hall.id)}>100 koltuğu tamamla</button></div>) : movies.map((movie) => <div key={movie.id}><span><b>{movie.title}</b><small>{movie.genre} · {movie.durationMinutes} dk</small></span><button onClick={() => removeMovie(movie.id)}>Sil</button></div>)}</section></div></div>;
}

function ScreeningDeletePanel({ movies, screenings, onChanged, showToast }: { movies: Movie[]; screenings: Screening[]; onChanged: () => void; showToast: (message: string) => void }) {
  async function removeScreening(id: number) {
    try {
      await apiRequest(`/api/admin/screenings/${id}`, { method: "DELETE" });
      showToast("Seans ve bağlı kayıtları silindi.");
      window.setTimeout(onChanged, 500);
    } catch (error) {
      showToast(error instanceof Error ? error.message : "Seans silinemedi");
    }
  }
  return <section className="screening-delete-panel"><div><span>SEANS YÖNETİMİ</span><h2>Mevcut seanslar</h2><p>Bir seansı sildiğinde ona bağlı rezervasyon, ödeme ve bilet kayıtları da temizlenir.</p></div><div className="screening-delete-list">{screenings.length ? screenings.map((screening) => { const movie = movies.find((item) => item.id === screening.movieId); return <article key={screening.id}><span><b>{movie?.title || `Film #${screening.movieId}`}</b><small>{formatScreeningDate(screening.startTime)} · Salon {screening.hallId}</small></span><button onClick={() => removeScreening(screening.id)}>Seansı sil</button></article>; }) : <p>Henüz seans bulunmuyor.</p>}</div></section>;
}

function SeatModal({ movie, screening, seats, selected, occupied, onToggle, onClose, onContinue }: { movie: Movie; screening: Screening; seats: Seat[]; selected: number[]; occupied: number[]; onToggle: (id: number) => void; onClose: () => void; onContinue: () => void }) {
  const rows = Array.from(new Set(seats.map((seat) => seat.rowNumber))).sort((a, b) => a - b);
  return <div className="modal-backdrop"><div className="seat-modal"><button className="modal-close" onClick={onClose}>×</button><div className="seat-header"><div><span>KOLTUK SEÇİMİ</span><h2>{movie.title}</h2><p>{formatScreeningDate(screening.startTime)} · Salon {screening.hallId}</p></div><div><strong>{selected.length * screening.price} ₺</strong><span>{selected.length} koltuk</span></div></div><div className="cinema-screen"><span>PERDE</span></div>{seats.length ? <div className="seats">{rows.map((row) => <div className="seat-row dynamic" key={row}><b>{seatRows[row - 1] || row}</b>{seats.filter((seat) => seat.rowNumber === row).sort((a, b) => a.seatNumber - b.seatNumber).map((seat) => { const isOccupied = occupied.includes(seat.id); return <button key={seat.id} className={isOccupied ? "occupied" : selected.includes(seat.id) ? "selected" : ""} disabled={isOccupied} onClick={() => onToggle(seat.id)} aria-label={`${row}. sıra ${seat.seatNumber}. koltuk${isOccupied ? " dolu" : ""}`}>{seat.seatNumber}</button>; })}</div>)}</div> : <div className="no-seats">Bu salon için henüz koltuk oluşturulmamış. Admin panelinden koltuk ekleyin.</div>}<div className="seat-legend"><span><i />Boş</span><span><i className="chosen" />Seçili</span><span><i className="busy" />Dolu</span></div><button className="primary seat-continue" disabled={!seats.length} onClick={onContinue}>Rezervasyona devam et <span>→</span></button></div></div>;
}
