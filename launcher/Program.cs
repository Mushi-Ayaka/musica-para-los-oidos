using System;
using System.IO;
using System.Net;
using System.Text;
using System.Threading;
using System.Diagnostics;
using System.Reflection;
using System.Windows.Forms;
using System.Drawing;
using System.IO.Compression;

namespace MusicaParaLosOidosLauncher
{
    static class Program
    {
        private static HttpListener listener;
        private static string contentFolder;
        private static int port;
        private static NotifyIcon trayIcon;
        private static Process browserProcess;

        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            try
            {
                // Extraer archivos estaticos si vienen embebidos
                PrepareContentFolder();

                // Iniciar servidor HTTP local
                StartHttpServer();

                // Abrir en ventana de aplicacion
                LaunchAppWindow();

                // Configurar icono en la bandeja del sistema (System Tray)
                SetupTrayIcon();

                Application.Run();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error al iniciar Musica para los Oidos: " + ex.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Cleanup();
            }
        }

        private static void PrepareContentFolder()
        {
            contentFolder = Path.Combine(Path.GetTempPath(), "MusicaParaLosOidos_v1");

            // Buscar si existe carpeta 'dist' relativa al ejecutable
            string localDist = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "dist");
            if (Directory.Exists(localDist) && File.Exists(Path.Combine(localDist, "index.html")))
            {
                contentFolder = localDist;
                return;
            }

            string webDist = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "..", "web", "dist");
            if (Directory.Exists(webDist) && File.Exists(Path.Combine(webDist, "index.html")))
            {
                contentFolder = Path.GetFullPath(webDist);
                return;
            }

            // Si viene como recurso incrustado en el ensamblado
            var asm = Assembly.GetExecutingAssembly();
            using (var stream = asm.GetManifestResourceStream("dist.zip"))
            {
                if (stream != null)
                {
                    if (Directory.Exists(contentFolder))
                    {
                        try { Directory.Delete(contentFolder, true); } catch { }
                    }
                    Directory.CreateDirectory(contentFolder);

                    string tempZip = Path.Combine(Path.GetTempPath(), "dist_" + Guid.NewGuid().ToString("N") + ".zip");
                    using (var fs = new FileStream(tempZip, FileMode.Create))
                    {
                        stream.CopyTo(fs);
                    }

                    ZipFile.ExtractToDirectory(tempZip, contentFolder);
                    try { File.Delete(tempZip); } catch { }
                }
            }
        }

        private static void StartHttpServer()
        {
            // Buscar puerto libre
            var tcpListener = new System.Net.Sockets.TcpListener(IPAddress.Loopback, 0);
            tcpListener.Start();
            port = ((IPEndPoint)tcpListener.LocalEndpoint).Port;
            tcpListener.Stop();

            listener = new HttpListener();
            listener.Prefixes.Add("http://127.0.0.1:" + port + "/");
            listener.Start();

            ThreadPool.QueueUserWorkItem(ListenLoop);
        }

        private static void ListenLoop(object state)
        {
            while (listener != null && listener.IsListening)
            {
                try
                {
                    var ctx = listener.GetContext();
                    ThreadPool.QueueUserWorkItem((c) => ProcessRequest((HttpListenerContext)c), ctx);
                }
                catch
                {
                    break;
                }
            }
        }

        private static void ProcessRequest(HttpListenerContext ctx)
        {
            try
            {
                string rawUrl = ctx.Request.Url.AbsolutePath;
                if (string.IsNullOrEmpty(rawUrl) || rawUrl == "/") rawUrl = "/index.html";

                string relativePath = rawUrl.TrimStart('/').Replace('/', Path.DirectorySeparatorChar);
                string filePath = Path.Combine(contentFolder, relativePath);

                if (!File.Exists(filePath))
                {
                    filePath = Path.Combine(contentFolder, "index.html");
                }

                if (File.Exists(filePath))
                {
                    byte[] data = File.ReadAllBytes(filePath);
                    ctx.Response.ContentType = GetMimeType(filePath);
                    ctx.Response.ContentLength64 = data.Length;
                    ctx.Response.AddHeader("Cache-Control", "no-cache");
                    ctx.Response.OutputStream.Write(data, 0, data.Length);
                }
                else
                {
                    ctx.Response.StatusCode = 404;
                }
            }
            catch
            {
                ctx.Response.StatusCode = 500;
            }
            finally
            {
                try { ctx.Response.Close(); } catch { }
            }
        }

        private static string GetMimeType(string path)
        {
            string ext = Path.GetExtension(path).ToLowerInvariant();
            switch (ext)
            {
                case ".html": return "text/html; charset=utf-8";
                case ".js":
                case ".mjs": return "application/javascript; charset=utf-8";
                case ".css": return "text/css; charset=utf-8";
                case ".json": return "application/json";
                case ".svg": return "image/svg+xml";
                case ".png": return "image/png";
                case ".jpg":
                case ".jpeg": return "image/jpeg";
                case ".webp": return "image/webp";
                case ".mp3": return "audio/mpeg";
                case ".ico": return "image/x-icon";
                default: return "application/octet-stream";
            }
        }

        private static void LaunchAppWindow()
        {
            string url = "http://127.0.0.1:" + port + "/";

            // Rutas de Edge o Chrome para modo aplicacion nativa
            string edgePath = @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe";
            if (!File.Exists(edgePath))
            {
                edgePath = @"C:\Program Files\Microsoft\Edge\Application\msedge.exe";
            }

            string chromePath = @"C:\Program Files\Google\Chrome\Application\chrome.exe";
            if (!File.Exists(chromePath))
            {
                chromePath = @"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe";
            }

            if (File.Exists(edgePath))
            {
                browserProcess = Process.Start(new ProcessStartInfo
                {
                    FileName = edgePath,
                    Arguments = "--app=\"" + url + "\" --window-size=430,920 --user-data-dir=\"" + Path.Combine(Path.GetTempPath(), "MusicaParaLosOidos_Profile") + "\"",
                    UseShellExecute = false
                });
            }
            else if (File.Exists(chromePath))
            {
                browserProcess = Process.Start(new ProcessStartInfo
                {
                    FileName = chromePath,
                    Arguments = "--app=\"" + url + "\" --window-size=430,920 --user-data-dir=\"" + Path.Combine(Path.GetTempPath(), "MusicaParaLosOidos_Profile") + "\"",
                    UseShellExecute = false
                });
            }
            else
            {
                Process.Start(url);
            }
        }

        private static void SetupTrayIcon()
        {
            var menu = new ContextMenu();
            menu.MenuItems.Add("Abrir Musica para los Oidos", (s, e) => LaunchAppWindow());
            menu.MenuItems.Add("-");
            menu.MenuItems.Add("Salir", (s, e) => {
                Cleanup();
                Application.Exit();
            });

            trayIcon = new NotifyIcon
            {
                Icon = SystemIcons.Application,
                ContextMenu = menu,
                Text = "Musica para los Oidos",
                Visible = true
            };

            trayIcon.DoubleClick += (s, e) => LaunchAppWindow();
        }

        private static void Cleanup()
        {
            if (trayIcon != null)
            {
                trayIcon.Visible = false;
                trayIcon.Dispose();
                trayIcon = null;
            }
            if (listener != null)
            {
                try { listener.Stop(); } catch { }
                try { listener.Close(); } catch { }
                listener = null;
            }
        }
    }
}
