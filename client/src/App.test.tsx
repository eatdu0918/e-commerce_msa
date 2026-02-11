import { render, screen } from '@testing-library/react'
import App from './App'
import { describe, it, expect, vi } from 'vitest'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { HelmetProvider } from 'react-helmet-async'

// Mock components that might cause issues in simple rendering
vi.mock('./components/home/HomePage', () => ({ default: () => <div>Home Page</div> }))
vi.mock('./components/layout/RootLayout', () => ({ default: ({ children }: { children: React.ReactNode }) => <div>{children}</div> }))

// Mock getMyProfile to avoid network calls
vi.mock('./api/services/user', () => ({
    getMyProfile: vi.fn(),
}))

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: false,
        },
    },
})

describe('App', () => {
    it('renders without crashing', () => {
        render(
            <QueryClientProvider client={queryClient}>
                <HelmetProvider>
                    <BrowserRouter>
                        <App />
                    </BrowserRouter>
                </HelmetProvider>
            </QueryClientProvider>
        )
        // Since App has Suspense and lazy loading, we might see the loading spinner first
        // or if we mocked everything, we might see the Home Page if the route matches /
        // For a basic test, just checking if it doesn't throw is good start.
    })
})
