import { Link } from "react-router-dom";

function Navbar() {
  return (
    <header className="bg-white shadow-sm">
      <div className="container mx-auto flex items-center justify-between px-4 py-4">
        <Link to="/" className="text-xl font-bold text-sky-700">
          Seafood Store
        </Link>
        <nav className="space-x-4 text-slate-700">
          <Link to="/">Home</Link>
          <Link to="/admin">Admin</Link>
        </nav>
      </div>
    </header>
  );
}

export default Navbar;
