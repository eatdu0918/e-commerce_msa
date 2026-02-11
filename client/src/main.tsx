import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import { Toaster } from 'react-hot-toast';
import './index.css'
import './i18n';
import App from './App.tsx'
import ErrorBoundary from './components/common/ErrorBoundary';

const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <HelmetProvider>
        <ErrorBoundary>
          <BrowserRouter>
            <App />
            <Toaster position="top-center" />
          </BrowserRouter>
        </ErrorBoundary>
      </HelmetProvider>
    </QueryClientProvider>
  </StrictMode>,
)
