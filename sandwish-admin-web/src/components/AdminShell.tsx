export function AdminShell() {
    return (
        <div className="admin-shell">
            <aside className="sidebar">
                <div className="brand">
                    <span className="brand-mark">S</span>
                    <div>
                        <strong>Sandwich</strong>
                        <span>Admin Console</span>
                    </div>
                </div>

                <nav className="nav-list" aria-label="后台导航">
                    <a className="nav-item active" href="#">
                        概览
                    </a>
                    <a className="nav-item" href="#">
                        用户管理
                    </a>
                    <a className="nav-item" href="#">
                        角色权限
                    </a>
                    <a className="nav-item" href="#">
                        系统日志
                    </a>
                    <a className="nav-item" href="#">
                        存储管理
                    </a>
                </nav>
            </aside>

            <main className="workspace">
                <header className="topbar">
                    <div>
                        <p className="eyebrow">admin-api workspace</p>
                        <h1>后台管理台</h1>
                    </div>
                    <button className="ghost-button" type="button">
                        连接检查
                    </button>
                </header>

                <section className="metrics" aria-label="核心指标">
                    <article className="metric-card">
                        <span>在线会话</span>
                        <strong>--</strong>
                    </article>
                    <article className="metric-card">
                        <span>待处理日志</span>
                        <strong>--</strong>
                    </article>
                    <article className="metric-card">
                        <span>存储对象</span>
                        <strong>--</strong>
                    </article>
                </section>

                <section className="panel">
                    <div>
                        <p className="eyebrow">getting started</p>
                        <h2>前端工程已就绪</h2>
                        <p>
                            这里是管理端的应用壳，后续可以接入登录、路由、权限菜单和
                            <code>sandwish-admin-api</code> 的业务接口。
                        </p>
                    </div>
                </section>
            </main>
        </div>
    );
}
