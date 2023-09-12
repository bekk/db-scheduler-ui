import ReactDOM from 'react-dom/client';
import App from './src/App';
import { BrowserRouter } from 'react-router-dom';

console.log('Hello from db-scheduler-ui-frontend/main.tsx');
ReactDOM.createRoot(document.getElementById('root')!).render(
  <BrowserRouter basename="db-scheduler">
    <App />
  </BrowserRouter>,
);
