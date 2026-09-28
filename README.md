# Kanalixor Radio

Kanalixor Radio, GitHub uzerindeki RADYOLAR.m3u listesini okuyan ve Media3 ile arka planda radyo oynatan Android uygulamasidir.

Liste adresi:

```text
https://raw.githubusercontent.com/Samim1971/box/refs/heads/main/RADYOLAR.m3u
```

## Ozellikler

- Sabit M3U listesini internetten otomatik indirme
- GitHub acilamazsa APK icindeki gomulu listeyi kullanma
- Listeyi yenileme
- `#EXTINF` isimlerini okuma
- HTTP/HTTPS radyo stream adreslerini listeleme
- Kanal secince oynatma
- Oynat, duraklat ve durdur kontrolleri
- GitHub Releases uzerinden Kanalixor Radio guncelleme kontrolu
- Arka plan ve kilit ekrani medya kontrolleri

## Gelistirme

```text
C:\PythonProject\KanalixorRadio
```

Test ve debug APK:

Ana calisma klasorunde:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Basarili olursa APK burada olusur:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Imzali release APK:

```powershell
Copy-Item keystore.properties.example keystore.properties
# keystore.properties icindeki yolu mevcut 0.5.6 imza anahtarina gore duzenleyin.
.\gradlew.bat assembleRelease
```

`keystore.properties` Git'e eklenmez. Mevcut kullanicilarin 0.5.6 uzerine guncelleme kurabilmesi icin ayni imza anahtari kullanilmalidir.

Yayin adiyla APK ve SHA-256 dosyasi hazirlamak icin:

```powershell
.\scripts\prepare-release.ps1
```

## Guncelleme sozlesmesi

- APK adi `Kanalixor-Radio-X.Y.Z.apk` olmalidir.
- Surum GitHub Releases altinda yayinlanmalidir.
- Uygulama ayni depodaki IPTV ve diger APK'lari guncelleme olarak kabul etmez.

## Liste davranisi

Uygulama acilinca liste otomatik yuklenir. GitHub acilamazsa APK icindeki son indirilen liste kullanilir. GitHub listesini guncellersen uygulamadaki `Listeyi yenile` dugmesine basman yeterli.
