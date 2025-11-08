# Manuale di Deploy per VoiceTombola

## 1. Preparazione dei file

Assicurati che la cartella `deploy/` contenga almeno questi file:
- `index.html`
- `manifest.json`
- `sw.js`
- `tombola.png`
- (eventuali altri file audio/immagini necessari)

## 2. Caricamento su Netlify

1. Vai su [https://app.netlify.com/](https://app.netlify.com/)
2. Accedi o registrati (puoi usare Google, GitHub, email, ecc.)
3. Clicca su **Add new site** > **Import an existing project**
4. Scegli **Deploy manually** (Drag & Drop)
5. Trascina la cartella `deploy/` (o seleziona tutti i file al suo interno) nell’area indicata
6. Attendi il completamento del caricamento
7. Netlify ti fornirà un link pubblico (es: `https://voicetombola-xxxx.netlify.app`)

## 3. Installazione come PWA su Android/iOS

1. Apri il link Netlify dal browser del telefono (consigliato Chrome su Android, Safari su iOS)
2. Attendi il caricamento completo
3. Dovresti vedere l’icona “Installa app” o “Aggiungi a schermata Home”
   - Su Chrome: menu ⋮ > “Installa app”
   - Su Safari: condividi > “Aggiungi a Home”
4. L’app sarà ora accessibile come PWA offline

## 4. Risoluzione problemi comuni

- Se non vedi l’opzione di installazione:
  - Verifica che il sito sia HTTPS (Netlify lo è di default)
  - Controlla che `manifest.json` e `sw.js` siano nella root della cartella deploy
  - Svuota la cache del browser e ricarica la pagina
- Se il microfono non funziona:
  - Consenti l’accesso al microfono quando richiesto
  - Se nega il permesso, vai nelle impostazioni del browser e consenti manualmente

## 5. Aggiornamenti

Per aggiornare l’app:
- Modifica i file nella cartella `deploy/`
- Ricarica nuovamente la cartella su Netlify (sovrascrivi la versione precedente)
- Svuota la cache sul telefono se non vedi i cambiamenti

---

Per assistenza, contatta lo sviluppatore o consulta la documentazione Netlify.