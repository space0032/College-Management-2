import React from 'react';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.group('🚀 App Crash Caught by ErrorBoundary');
        console.error('Error:', error);
        console.error('Component Stack:', errorInfo.componentStack);
        console.groupEnd();
    }

    render() {
        if (this.state.hasError) {
            return (
                <div style={{
                    padding: '40px',
                    textAlign: 'center',
                    background: '#f8fafc',
                    minHeight: '100vh',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontFamily: 'system-ui, -apple-system, sans-serif'
                }}>
                    <div style={{ fontSize: '4rem', marginBottom: '20px' }}>⚡</div>
                    <h1 style={{ color: '#1e293b', marginBottom: '10px' }}>Something went wrong.</h1>
                    <p style={{ color: '#64748b', maxWidth: '400px', marginBottom: '30px', lineHeight: '1.6' }}>
                        The application encountered an unexpected UI error. Your data is safe, but this specific view failed to render.
                    </p>
                    <div style={{ display: 'flex', gap: '12px' }}>
                        <button
                            onClick={() => window.location.reload()}
                            style={{
                                padding: '12px 24px',
                                background: '#6366f1',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                fontWeight: '600',
                                cursor: 'pointer'
                            }}
                        >
                            Reload Application
                        </button>
                        <button
                            onClick={() => this.setState({ hasError: false })}
                            style={{
                                padding: '12px 24px',
                                background: '#e2e8f0',
                                color: '#475569',
                                border: 'none',
                                borderRadius: '8px',
                                fontWeight: '600',
                                cursor: 'pointer'
                            }}
                        >
                            Try Again
                        </button>
                    </div>
                    {process.env.NODE_ENV === 'development' && (
                        <pre style={{
                            marginTop: '40px',
                            padding: '20px',
                            background: '#fff',
                            border: '1px solid #e2e8f0',
                            borderRadius: '8px',
                            textAlign: 'left',
                            maxWidth: '800px',
                            overflow: 'auto',
                            fontSize: '12px',
                            color: '#ef4444'
                        }}>
                            {this.state.error?.toString()}
                        </pre>
                    )}
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
