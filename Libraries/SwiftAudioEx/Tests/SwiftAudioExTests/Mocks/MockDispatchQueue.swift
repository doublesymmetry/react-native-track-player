//
//  MockDispatchQueue.swift
//  SwiftAudio_Tests
//
//  Created by David Chavez on 29.05.21.
//  Copyright © 2021 Double Symmmery. All rights reserved.
//

import Foundation

@testable import SwiftAudioEx

final class MockDispatchQueue: DispatchQueueType {
    func async(flags: DispatchWorkItemFlags, execute work: @escaping @convention(block) () -> Void) {
        work()
    }
}
