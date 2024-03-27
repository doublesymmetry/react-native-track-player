//
//  QueueTableViewCell.swift
//  SwiftAudio_Example
//
//  Created by Jørgen Henrichsen on 25/03/2018.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import UIKit

class QueueTableViewCell: UITableViewCell {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var artistLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
}
