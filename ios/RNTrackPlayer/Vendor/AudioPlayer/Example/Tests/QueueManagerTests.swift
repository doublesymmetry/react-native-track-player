import Quick
import Nimble

@testable import SwiftAudio


class QueueManagerTests: QuickSpec {
    
    let dummyItem = 0
    
    let dummyItems: [Int] = [0, 1, 2, 3, 4, 5, 6]
    
    override func spec() {
        
        describe("A QueueManager") {
            
            var manager: QueueManager<Int>!
            
            beforeEach {
                manager = QueueManager()
            }
            
            describe("its current item", {
                
                it("should be nil", closure: {
                    expect(manager.current).to(beNil())
                })
                
                context("when one item is added", closure: {
                    beforeEach {
                        manager.addItem(self.dummyItem)
                    }
                    
                    it("should not be nil", closure: {
                        expect(manager.current).toNot(beNil())
                    })
                    
                    it("should be the added item", closure: {
                        expect(manager.current).to(equal(self.dummyItem))
                    })
                    
                    context("then replaced", closure: {
                        beforeEach {
                            manager.replaceCurrentItem(with: 1)
                        }
                        it("should be the new item", closure: {
                            expect(manager.current).to(equal(1))
                        })
                    })
                })
                
                context("when replaced", closure: {
                    beforeEach {
                        manager.replaceCurrentItem(with: 1)
                    }
                    
                    it("should not be nil", closure: {
                        expect(manager.current).toNot(beNil())
                    })
                })
                
                context("when mulitple items are added", {
                    beforeEach {
                        manager.addItems(self.dummyItems)
                    }
                    
                    it("should not be nil", closure: {
                        expect(manager.current).toNot(beNil())
                    })
                })
                
            })
            
            context("when adding one item", {
                
                beforeEach {
                    manager.addItem(self.dummyItem)
                }
                
                it("should have an item in the queue", closure: {
                    expect(manager.items).notTo(beEmpty())
                })
                
                context("then replacing the item", closure: {
                    beforeEach {
                        manager.replaceCurrentItem(with: 1)
                    }
                    it("should have replaced the current item", closure: {
                        expect(manager.current).to(equal(1))
                    })
                })
                
                context("then calling next", {
                    
                    var nextItem: Int?
                    
                    beforeEach {
                        nextItem = try? manager.next()
                    }
                    
                    it("should not return", closure: {
                        expect(nextItem).to(beNil())
                    })
                    
                })
                
                context("then calling previous", {
                    var previousItem: Int?
                    
                    beforeEach {
                        previousItem = try? manager.previous()
                    }
                    
                    it("should not return", closure: {
                        expect(previousItem).to(beNil())
                    })
                })
                
            })
            
            context("when adding multiple items", {
                
                beforeEach {
                    manager.addItems(self.dummyItems)
                }
                
                it("should have items in the queue", closure: {
                    expect(manager.items.count).to(equal(self.dummyItems.count))
                })
                
                it("should have the first item as a current item", closure: {
                    expect(manager.current).toNot(beNil())
                    expect(manager.current).to(equal(self.dummyItems.first))
                })
                
                it("should have next items", closure: {
                    expect(manager.nextItems).toNot(beNil())
                    expect(manager.nextItems.count).to(equal(self.dummyItems.count - 1))
                })
                
                context("then calling next", {
                    var nextItem: Int?
                    beforeEach {
                        nextItem = try? manager.next()
                    }
                    
                    it("should return the next item", closure: {
                        expect(nextItem).toNot(beNil())
                        expect(nextItem).to(equal(self.dummyItems[1]))
                    })
                    
                    it("should have next current item", closure: {
                        expect(manager.current).to(equal(self.dummyItems[1]))
                    })
                    
                    it("should have previous items", closure: {
                        expect(manager.previousItems).toNot(beNil())
                    })
                    
                    context("then calling previous", {
                        var previousItem: Int?
                        beforeEach {
                            previousItem = try? manager.previous()
                        }
                        it("should return the first item", closure: {
                            expect(previousItem).toNot(beNil())
                            expect(previousItem).to(equal(self.dummyItems.first))
                        })
                        it("should have the previous current item", closure: {
                            expect(manager.current).to(equal(self.dummyItems.first))
                        })
                    })
                    
                    context("then removing previous items", {
                        beforeEach {
                            manager.removePreviousItems()
                        }
                        it("should have no previous items", closure: {
                            expect(manager.previousItems.count).to(equal(0))
                        })
                        it("should have current index zero", closure: {
                            expect(manager.currentIndex).to(equal(0))
                        })
                    })
                })
                
                context("adding more items", {
                    var initialItemCount: Int!
                    let newItems: [Int] = [10, 11, 12, 13]
                    beforeEach {
                        initialItemCount = manager.items.count
                        try? manager.addItems(newItems, at: manager.items.endIndex - 1)
                    }
                    
                    it("should have more items", closure: {
                        expect(manager.items.count).to(equal(initialItemCount + newItems.count))
                    })
                })
                
                context("adding more items at a smaller index than currentIndex", {
                    var initialCurrentIndex: Int!
                    let newItems: [Int] = [10, 11, 12, 13]
                    beforeEach {
                        initialCurrentIndex = manager.currentIndex
                        try? manager.addItems(newItems, at: initialCurrentIndex)
                    }
                    
                    it("currentIndex should increase by number of new items", closure: {
                        expect(manager.currentIndex).to(equal(initialCurrentIndex + newItems.count))
                    })
                })
                
                // MARK: - Removal
                
                context("then removing a item with index less than currentIndex", {
                    beforeEach {
                        var removed: Int?
                        var initialCurrentIndex: Int!
                        beforeEach {
                            let _ = try? manager.jump(to: 3)
                            initialCurrentIndex = manager.currentIndex
                            removed = try? manager.removeItem(at: initialCurrentIndex - 1)
                        }
                        
                        it("should remove an item", closure: {
                            expect(removed).toNot(beNil())
                        })
                        
                        it("should decrement the currentIndex", closure: {
                            expect(manager.currentIndex).to(equal(initialCurrentIndex - 1))
                        })
                    }
                })
                
                context("then removing the second item", {
                    var removed: Int?
                    beforeEach {
                        removed = try? manager.removeItem(at: 1)
                    }
                    
                    it("should have one less item", closure: {
                        expect(removed).toNot(beNil())
                        expect(manager.items.count).to(equal(self.dummyItems.count - 1))
                    })
                })
                
                context("then removing the last item", {
                    var removed: Int?
                    beforeEach {
                        removed = try? manager.removeItem(at: self.dummyItems.count - 1)
                    }
                    
                    it("should have one less item", closure: {
                        expect(removed).toNot(beNil())
                        expect(manager.items.count).to(equal(self.dummyItems.count - 1))
                    })
                })
                
                context("then removing the current item", {
                    var removed: Int?
                    beforeEach {
                        removed = try? manager.removeItem(at: manager.currentIndex)
                    }
                    it("should not remove any items", closure: {
                        expect(removed).to(beNil())
                        expect(manager.items.count).to(equal(self.dummyItems.count))
                    })
                })
                
                context("then removing with too large index", {
                    var removed: Int?
                    beforeEach {
                        removed = try? manager.removeItem(at: self.dummyItems.count)
                    }

                    it("should not remove any items", closure: {
                        expect(removed).to(beNil())
                        expect(manager.items.count).to(equal(self.dummyItems.count))
                    })
                })
                
                context("then removing with too small index", {
                    var removed: Int?
                    beforeEach {
                        removed = try? manager.removeItem(at: -1)
                    }
                    
                    it("should not remove any items", closure: {
                        expect(removed).to(beNil())
                        expect(manager.items.count).to(equal(self.dummyItems.count))
                    })
                })
                
                context("then removing upcoming items", {
                    beforeEach {
                        manager.removeUpcomingItems()
                    }
                    
                    it("should have no next items", closure: {
                        expect(manager.nextItems.count).to(equal(0))
                    })
                })
                
                // MARK: - Jumping
                
                context("then jumping to the current item", {
                    var error: Error?
                    var item: Int?
                    beforeEach {
                        do {
                            item = try manager.jump(to: manager.currentIndex)
                        }
                        catch let err {
                            error = err
                        }
                    }
                    
                    it("should not return an item", closure: {
                        expect(item).to(beNil())
                    })
                    
                    it("should throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("then jumping to the second item", {
                    var jumped: Int?
                    beforeEach {
                        try? jumped = manager.jump(to: 1)
                    }
                    
                    it("should return the current item", closure: {
                        expect(jumped).toNot(beNil())
                        expect(jumped).to(equal(manager.current))
                    })
                    
                    it("should move the current index", closure: {
                        expect(manager.currentIndex).to(equal(1))
                    })
                })
                
                context("then jumping to last item", closure: {
                    var jumped: Int?
                    beforeEach {
                        try? jumped = manager.jump(to: manager.items.count - 1)
                    }
                    it("should return the current item", closure: {
                        expect(jumped).toNot(beNil())
                        expect(jumped).to(equal(manager.current))
                    })
                    
                    it("should move the current index", closure: {
                        expect(manager.currentIndex).to(equal(manager.items.count - 1))
                    })
                })
                
                context("then jumping to a negative index", closure: {
                    var jumped: Int?
                    beforeEach {
                        jumped = try? manager.jump(to: -1)
                    }
                    
                    it("should not return", closure: {
                        expect(jumped).to(beNil())
                    })
                    
                    it("should not move the current index", closure: {
                        expect(manager.currentIndex).to(equal(0))
                    })
                })
                
                context("then jumping with too large index", closure: {
                    var jumped: Int?
                    beforeEach {
                        jumped = try? manager.jump(to: manager.items.count)
                    }
                    it("should not return", closure: {
                        expect(jumped).to(beNil())
                    })
                    
                    it("should not move the current index", closure: {
                        expect(manager.currentIndex).to(equal(0))
                    })
                })
                
                // MARK: - Moving
                
                context("moving from current index", {
                    var error: Error?
                    beforeEach {
                        do {
                            try manager.moveItem(fromIndex: manager.currentIndex, toIndex: manager.currentIndex + 1)
                        }
                        catch let err { error = err }
                    }
                    
                    it("throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("moving from a negative index", {
                    var error: Error?
                    beforeEach {
                        do {
                            try manager.moveItem(fromIndex: -1, toIndex: manager.currentIndex + 1)
                        }
                        catch let err { error = err }
                    }
                    
                    it("should throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("moving from a too large index", {
                    var error: Error?
                    beforeEach {
                        do {
                            try manager.moveItem(fromIndex: manager.items.count, toIndex: manager.currentIndex + 1)
                        }
                        catch let err { error = err }
                    }
                    
                    it("should throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("moving to a negative index", {
                    var error: Error?
                    beforeEach {
                        do {
                            try manager.moveItem(fromIndex: manager.currentIndex + 1, toIndex: -1)
                        }
                        catch let err { error = err }
                    }
                    
                    it("should throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("moving to a too large index", {
                    var error: Error?
                    beforeEach {
                        do {
                            try manager.moveItem(fromIndex: manager.currentIndex + 1, toIndex: manager.items.count)
                        }
                        catch let err { error = err }
                    }
                    
                    it("should throw an error", closure: {
                        expect(error).toNot(beNil())
                    })
                })
                
                context("then moving 2nd to 4th", closure: {
                    let afterMoving: [Int] = [0, 2, 3, 1, 4, 5, 6]
                    beforeEach {
                        try? manager.moveItem(fromIndex: 1, toIndex: 3)
                    }
                    
                    it("should move the item", closure: {
                        expect(manager.items).to(equal(afterMoving))
                    })
                })
                
                // MARK: - Clear
                
                context("when queue is cleared", {
                    beforeEach {
                        manager.clearQueue()
                    }
                    
                    it("should have currentIndex 0", closure: {
                        expect(manager.currentIndex).to(equal(0))
                    })
                    
                    it("should have no items", closure: {
                        expect(manager.items.count).to(equal(0))
                    })
                })
            })
        }
    }
}
