# Monkey patch for kconv compatibility with Ruby 3.4
# kconv was removed from Ruby 3.4 standard library
if RUBY_VERSION >= "3.4.0"
  require 'nkf'
  module Kconv
    def self.toeuc(str)
      NKF.nkf('-We', str)
    end
    def self.tosjis(str)
      NKF.nkf('-Ws', str)
    end
    def self.tojis(str)
      NKF.nkf('-Wj', str)
    end
    def self.toutf8(str)
      NKF.nkf('-w', str)
    end
  end
end
