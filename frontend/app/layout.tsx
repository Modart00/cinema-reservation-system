import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cinema — Perde açılıyor",
  description: "Filmleri keşfet, seansını ve koltuğunu seç, biletini saniyeler içinde al.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="tr"><body>{children}</body></html>;
}
