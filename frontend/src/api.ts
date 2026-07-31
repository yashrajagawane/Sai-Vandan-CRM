const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';
export type User = { id: string; fullName: string; email: string; roles: string[] };
export type AuthResult = { accessToken: string; refreshToken: string; user: User };
export type Lead = { id: string; leadNumber: string; customerName: string; mobile: string; source: string; status: string; temperature: string; assignedTo: string | null; createdAt: string };
export type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number };
export type Dashboard = { role: string; title: string; subtitle: string; metrics: { label: string; value: string; note: string; money: boolean }[]; queue: { label: string; count: number; note: string }[]; modules: string[] };
export type Workspace = { module: string; title: string; role: string; rows: Record<string, string | number | null>[] };

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('sv_access_token');
  const response = await fetch(`${API_URL}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers } });
  if (!response.ok) { const error = await response.json().catch(() => ({})); throw new Error(error.message ?? 'Something went wrong.'); }
  return response.json() as Promise<T>;
}
export const api = {
  login: (email: string, password: string) => request<AuthResult>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  me: () => request<User>('/auth/me'),
  dashboard: () => request<Dashboard>('/dashboard'),
  workspace: (module: string) => request<Workspace>(`/workspace/${module}`),
  leads: () => request<Page<Lead>>('/leads?size=50'),
  createLead: (lead: Record<string, unknown>) => request<Lead>('/leads', { method: 'POST', body: JSON.stringify(lead) })
};
