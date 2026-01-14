# Android Geofencing
## Opis in utemelitev izbire
Android Geofencing API je del storitve Google Play Services Location in omogoča aplikacijam zaznavanje, kdaj uporabnik vstopi v ali iztopil vnaprej določeno geografsko območje (geofence). Tehnologija omogoča razvoj lokacijsko odvisnih funkcionalnosti in ne zahteva neprekinjenega spremljanja uporabnikove GPS lokacije.

Tehnologijo sem uporabil pri moji android aplikaciji Kulturni vodič, saj lahko uporabnika obvesti o atrakcijah v njegovi bljižini.

---

## Prednosti:
- Nizka poraba energije
- Delovanje v ozadju
- Enostavna Integracija


## Slabosti:
- Odvisnost od Google Play Services
- Omejeno število geofencev
- Zahteva dovoljenja
- Zamik pri zaznavanju
- Težja emulacija

## Licenca
Android Geofencing API je del Google Play Services, ki je na voljo pod Google APIs Terms of Service. Koda je licencirana pod licenco Apache 2.0, ki omogoča:

- prosto uporabo tehnologije za osebne, izobraževalne in komercialne namene
- spreminjanje in prilagajanje kode brez posebnih omejitev
- distribucijo programske opreme ob upoštevanju pogojev licence

## Uporabniki in razširjenost
Android Geofencing API je del operacijskega sistema Android, ki je na voljo na več kot 3 milijardah naprav.

- Uporabljajo ga večje aplikacije kot so Google Maps, in se mnogo drugih na Google Play Store

## Časovna in prostorska zahtevnost

| Lastnost | Ocena |
|--------|------|
| Časovna zahtevnost | O(1) na dogodek |
| Prostorska zahtevnost | O(n), kjer je n število geofence-ov |
| Poraba baterije | nizka |
| Poraba pomnilnika | zanemarljiva |

##  Vzdrževanje tehnologije
- Razvijalec: Google  
- Število razvijalcev: ni točno znano  
- Status: aktivno vzdrževano
- Zadnja sprememba: Maj 2024, različica 21.3.0

---
