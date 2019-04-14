require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-track-player"
  s.version      = package["version"]
  s.summary      = "A fully fledged audio module created for music apps"
  s.author       = "David Chavez"
  s.homepage     = "https://github.com/react-native-kit/react-native-track-player"
  s.license      = "Apache-2.0"
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/react-native-kit/react-native-track-player.git", :tag => "v#{s.version}" }
  s.source_files  = "ios/**/*.{h,m,swift}"
  s.dependency "React"
end
