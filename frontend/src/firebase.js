import { initializeApp } from 'firebase/app'
import { getAuth, GoogleAuthProvider } from 'firebase/auth'

const firebaseConfig = {
  apiKey: 'AIzaSyAzZBG9S2ztzblIpg0kOSOMl6T0D2asQFA',
  authDomain: 'vault-fs.firebaseapp.com',
  projectId: 'vault-fs',
  storageBucket: 'vault-fs.firebasestorage.app',
  messagingSenderId: '341499973695',
  appId: '1:341499973695:web:19e0aa47d8356ffef6288a'
}

const app = initializeApp(firebaseConfig)
const auth = getAuth(app)
const googleProvider = new GoogleAuthProvider()

export { auth, googleProvider }