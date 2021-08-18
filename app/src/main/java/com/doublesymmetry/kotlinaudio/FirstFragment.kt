package com.doublesymmetry.kotlinaudio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.doublesymmetry.kotlinaudio.databinding.FragmentFirstBinding
import com.doublesymmetry.kotlinaudio.models.DefaultAudioItem
import com.doublesymmetry.kotlinaudio.models.SourceType

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = DefaultAudioItem(
            "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3", SourceType.FILE,
            title = "Dirty Computer",
            artwork = "https://upload.wikimedia.org/wikipedia/en/0/0b/DirtyComputer.png",
            artist = "Janelle Monáe"
        )
        val player = QueuedAudioPlayer(requireActivity())
        player.add(item)
        player.add(item)
        player.play()

//        print(player.nextItems)
        Log.d("TEST", player.nextItems.toString())

        binding.buttonFirst.setOnClickListener {
            player.next()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}