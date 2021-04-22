#pragma once

#include <unknwn.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Foundation.Collections.h>
#include <winrt/Windows.Media.h>
#include <winrt/Windows.Media.Core.h>
#include <winrt/Windows.Media.Playback.h>
#include <winrt/Windows.Storage.Streams.h>
#include <winrt/Windows.System.h>

// This can be changed to 1 to activate verbose debug mode.
#if 0
#define VERBOSE_DEBUG OutputDebugStringA
#else
#define VERBOSE_DEBUG 
#endif