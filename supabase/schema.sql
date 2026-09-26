create table if not exists public.user_state (
  user_id uuid primary key references auth.users(id) on delete cascade,
  state jsonb not null default '{}'::jsonb,
  updated_at timestamptz not null default now()
);

alter table public.user_state enable row level security;

drop policy if exists "timewallet_select_own" on public.user_state;
drop policy if exists "timewallet_insert_own" on public.user_state;
drop policy if exists "timewallet_update_own" on public.user_state;

create policy "timewallet_select_own" on public.user_state for select using (auth.uid() = user_id);
create policy "timewallet_insert_own" on public.user_state for insert with check (auth.uid() = user_id);
create policy "timewallet_update_own" on public.user_state for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
