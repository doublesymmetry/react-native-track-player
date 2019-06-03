//
//  QueueViewController.swift
//  SwiftAudio_Example
//
//  Created by Jørgen Henrichsen on 25/03/2018.
//  Copyright © 2018 CocoaPods. All rights reserved.
//

import UIKit
import SwiftAudio


class QueueViewController: UIViewController {
    
    let controller = AudioController.shared
    @IBOutlet weak var tableView: UITableView!
    
    let cellReuseId: String = "QueueCell"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.register(UINib.init(nibName: "QueueTableViewCell", bundle: Bundle.main), forCellReuseIdentifier: cellReuseId)
        tableView.delegate = self
        tableView.dataSource = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBAction func closeButton(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }
    
}

extension QueueViewController: UITableViewDataSource, UITableViewDelegate {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 2
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch section {
        case 0:
            return 1
        case 1:
            return controller.player.nextItems.count
        default:
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellReuseId, for: indexPath) as! QueueTableViewCell
        
        let item: AudioItem?
        switch indexPath.section {
        case 0:
            item = controller.player.currentItem
        case 1:
            item = controller.player.nextItems[indexPath.row]
        default:
            item = nil
        }
        
        if let item = item {
            cell.titleLabel.text = item.getTitle()
            cell.artistLabel.text = item.getArtist()
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        switch section {
        case 0: return "Playing Now"
        case 1: return "Up Next"
        default: return nil
        }
    }
    
}
