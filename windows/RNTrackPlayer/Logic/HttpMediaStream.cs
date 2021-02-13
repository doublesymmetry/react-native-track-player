using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Media.Core;
using Windows.Storage.Streams;
using Windows.Web.Http;

namespace RNTrackPlayer.Logic
{
    class HttpMediaStream : IRandomAccessStreamWithContentType
    {
        private HttpClient client;
        private IInputStream inputStream;
        private ulong size;
        private string etagHeader;
        private string lastModifiedHeader;
        private Uri requestedUri;
        private string contentType = string.Empty;
        private ulong requestedPosition;

        public bool CanRead => true;

        public bool CanWrite => false;

        public ulong Position { get { return requestedPosition; } }

        public ulong Size
        {
            get { return size; }
            set { throw new NotImplementedException(); }
        }

        public string ContentType
        {
            get { return contentType; }
            private set { contentType = value; }
        }

        private HttpMediaStream(HttpClient client, Uri uri)
        {
            this.client = client;
            requestedUri = uri;
            requestedPosition = 0;
        }

        private async Task SendRequesAsync()
        {
            Debug.Assert(inputStream == null);

            HttpRequestMessage request = null;
            request = new HttpRequestMessage(HttpMethod.Get, requestedUri);

            request.Headers.Add("Range", String.Format("bytes={0}-", requestedPosition));

            if (!String.IsNullOrEmpty(etagHeader))
            {
                request.Headers.Add("If-Match", etagHeader);
            }

            if (!String.IsNullOrEmpty(lastModifiedHeader))
            {
                request.Headers.Add("If-Unmodified-Since", lastModifiedHeader);
            }

            HttpResponseMessage response = await client.SendRequestAsync(
                request,
                HttpCompletionOption.ResponseHeadersRead).AsTask().ConfigureAwait(false);

            if (response.Content.Headers.ContentType != null)
            {
                this.ContentType = response.Content.Headers.ContentType.MediaType;
            }

            size = response.Content.Headers.ContentLength.Value;

            if (response.StatusCode != HttpStatusCode.PartialContent && requestedPosition != 0)
            {
                throw new Exception("HTTP server did not reply with a '206 Partial Content' status.");
            }

            if (!response.Headers.ContainsKey("Accept-Ranges"))
            {
                throw new Exception(String.Format(
                    "HTTP server does not support range requests: {0}",
                    "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.5"));
            }

            if (String.IsNullOrEmpty(etagHeader) && response.Headers.ContainsKey("ETag"))
            {
                etagHeader = response.Headers["ETag"];
            }

            if (String.IsNullOrEmpty(lastModifiedHeader) && response.Content.Headers.ContainsKey("Last-Modified"))
            {
                lastModifiedHeader = response.Content.Headers["Last-Modified"];
            }
            if (response.Content.Headers.ContainsKey("Content-Type"))
            {
                contentType = response.Content.Headers["Content-Type"];
            }

            inputStream = await response.Content.ReadAsInputStreamAsync().AsTask().ConfigureAwait(false);
        }

        public static IAsyncOperation<HttpMediaStream> CreateAsync(HttpClient client, Uri uri)
        {
            HttpMediaStream randomStream = new HttpMediaStream(client, uri);

            return AsyncInfo.Run<HttpMediaStream>(async (cancellationToken) =>
            {
                await randomStream.SendRequesAsync().ConfigureAwait(false);
                return randomStream;
            });
        }

        public IInputStream GetInputStreamAt(ulong position)
        {
            throw new NotImplementedException();
        }

        public IOutputStream GetOutputStreamAt(ulong position)
        {
            throw new NotImplementedException();
        }

        public void Seek(ulong position)
        {
            if (requestedPosition != position)
            {
                if (inputStream != null)
                {
                    inputStream.Dispose();
                    inputStream = null;
                }
                Debug.WriteLine("Seek: {0:N0} -> {1:N0}", requestedPosition, position);
                requestedPosition = position;
            }
        }

        public IRandomAccessStream CloneStream()
        {
            // If there is only one MediaPlayerElement using the stream, it is safe to return itself.
            return this;
        }

        public IAsyncOperationWithProgress<IBuffer, uint> ReadAsync(IBuffer buffer, uint count, InputStreamOptions options)
        {
            return AsyncInfo.Run<IBuffer, uint>(async (cancellationToken, progress) =>
            {
                progress.Report(0);

                try
                {
                    if (inputStream == null)
                    {
                        await SendRequesAsync().ConfigureAwait(false);
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine(ex);
                    throw;
                }

                IBuffer result = await inputStream.ReadAsync(buffer, count, options).AsTask(cancellationToken, progress).ConfigureAwait(false);

                // Move position forward.
                requestedPosition += result.Length;
                Debug.WriteLine("requestedPosition = {0:N0}", requestedPosition);

                return result;
            });
        }

        public IAsyncOperationWithProgress<uint, uint> WriteAsync(IBuffer buffer)
        {
            throw new NotImplementedException();
        }

        public IAsyncOperation<bool> FlushAsync()
        {
            throw new NotImplementedException();
        }

        public void Dispose()
        {
            if (inputStream != null)
            {
                inputStream.Dispose();
                inputStream = null;
            }
        }
    }
}
